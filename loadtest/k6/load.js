import http from 'k6/http';
import { check } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8088';
const SEED_USERS = parseInt(__ENV.SEED_USERS || '20000', 10);
const FIRST_SEED_ID = parseInt(__ENV.FIRST_SEED_ID || '4', 10);
const TARGET_RPS = parseInt(__ENV.TARGET_RPS || '1000', 10);
const RAMP_UP = __ENV.RAMP_UP || '30s';
const HOLD = __ENV.HOLD || '2m';
const RESULTS_DIR = __ENV.RESULTS_DIR || '/results';
const SCENARIO = __ENV.SCENARIO || 'mixed';

const JSON_HEADERS = { 'Content-Type': 'application/json' };

http.setResponseCallback(http.expectedStatuses(200, 201));

export const options = {
    scenarios: {
        mixed: {
            executor: 'ramping-arrival-rate',
            startRate: Math.max(1, Math.floor(TARGET_RPS / 10)),
            timeUnit: '1s',
            preAllocatedVUs: Math.max(50, Math.floor(TARGET_RPS / 5)),
            maxVUs: Math.max(200, TARGET_RPS),
            stages: [
                { target: TARGET_RPS, duration: RAMP_UP },
                { target: TARGET_RPS, duration: HOLD },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<300'],
        'http_req_duration{endpoint:me}': ['p(95)<300'],
        'http_req_duration{endpoint:getById}': ['p(95)<300'],
        'http_req_duration{endpoint:search}': ['p(95)<300'],
        'http_req_duration{endpoint:transfer}': ['p(95)<500'],
        'http_req_duration{endpoint:credit}': ['p(95)<300'],
        'http_req_duration{endpoint:withdraw}': ['p(95)<300'],
        dropped_iterations: ['count<100'],
    },
    summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

let token = null;
let userIndex = 0;

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pad(value, length) {
    let result = String(value);
    while (result.length < length) {
        result = '0' + result;
    }
    return result;
}

function randomUserIndex() {
    return randomInt(1, SEED_USERS);
}

function userId(index) {
    return FIRST_SEED_ID + index - 1;
}

function authHeaders() {
    return { Authorization: `Bearer ${token}` };
}

function login() {
    userIndex = ((__VU - 1) % SEED_USERS) + 1;
    const response = http.post(
        `${BASE_URL}/api/auth/login`,
        JSON.stringify({ login: `load${userIndex}@example.com`, password: 'password123' }),
        { headers: JSON_HEADERS, tags: { endpoint: 'login' } },
    );
    check(response, { 'login 200': (r) => r.status === 200 });
    token = response.status === 200 ? response.body : null;
}

function getMe() {
    const response = http.get(`${BASE_URL}/api/users/me`, {
        headers: authHeaders(),
        tags: { endpoint: 'me' },
    });
    check(response, { 'me 200': (r) => r.status === 200 });
}

function getById() {
    const response = http.get(`${BASE_URL}/api/users/${userId(randomUserIndex())}`, {
        headers: authHeaders(),
        tags: { endpoint: 'getById', name: 'GET /api/users/{id}' },
    });
    check(response, { 'getById 200': (r) => r.status === 200 });
}

function search() {
    const variant = randomInt(1, 4);
    const page = randomInt(0, 2);
    let query;
    if (variant === 1) {
        query = `name=${encodeURIComponent(`Load User ${randomInt(1, 99)}`)}`;
    } else if (variant === 2) {
        query = `dateOfBirth=${pad(randomInt(1, 28), 2)}.${pad(randomInt(1, 12), 2)}.${randomInt(1960, 2000)}`;
    } else if (variant === 3) {
        query = `phone=7900${pad(randomUserIndex(), 7)}`;
    } else {
        query = `email=load${randomUserIndex()}@example.com`;
    }
    const response = http.get(`${BASE_URL}/api/users?${query}&page=${page}&size=20`, {
        headers: authHeaders(),
        tags: { endpoint: 'search', name: 'GET /api/users?…' },
    });
    check(response, { 'search 200': (r) => r.status === 200 });
}

function transfer() {
    let target = randomUserIndex();
    if (target === userIndex) {
        target = (target % SEED_USERS) + 1;
    }
    const response = http.post(
        `${BASE_URL}/api/transfers`,
        JSON.stringify({ userId: userId(target), value: '1.00' }),
        { headers: Object.assign({}, JSON_HEADERS, authHeaders()), tags: { endpoint: 'transfer' } },
    );
    check(response, { 'transfer 200': (r) => r.status === 200 });
}

function changeBalance(operation) {
    const response = http.post(
        `${BASE_URL}/api/account/${operation}`,
        JSON.stringify({ value: '1.00' }),
        { headers: Object.assign({}, JSON_HEADERS, authHeaders()), tags: { endpoint: operation } },
    );
    check(response, { [`${operation} 200`]: (r) => r.status === 200 });
}

function mixed() {
    const roll = Math.random();
    if (roll < 0.35) {
        getMe();
    } else if (roll < 0.45) {
        getById();
    } else if (roll < 0.65) {
        search();
    } else if (roll < 0.75) {
        transfer();
    } else if (roll < 0.875) {
        changeBalance('credit');
    } else {
        changeBalance('withdraw');
    }
}

function balance() {
    changeBalance(Math.random() < 0.5 ? 'credit' : 'withdraw');
}

const SCENARIOS = { mixed, balance };

if (!SCENARIOS[SCENARIO]) {
    throw new Error(`Unknown SCENARIO: ${SCENARIO}, expected one of: ${Object.keys(SCENARIOS).join(', ')}`);
}

export default function () {
    if (!token) {
        login();
        return;
    }
    SCENARIOS[SCENARIO]();
}

export function handleSummary(data) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const baseName = `${RESULTS_DIR}/summary-${timestamp}-${SCENARIO}-rps${TARGET_RPS}`;
    return {
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
        [`${baseName}.txt`]: textSummary(data, { indent: ' ', enableColors: false }),
        [`${baseName}.json`]: JSON.stringify(data, null, 2),
    };
}
