param(
    [ValidateSet("mixed", "balance")]
    [string]$Scenario = "balance",
    [int]$TargetRps = 10000,
    [string]$RampUp = "30s",
    [string]$Hold = "2m"
)

$names = "SCENARIO", "TARGET_RPS", "RAMP_UP", "HOLD"
$previous = @{}
foreach ($name in $names) {
    $previous[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

$env:SCENARIO = $Scenario
$env:TARGET_RPS = $TargetRps
$env:RAMP_UP = $RampUp
$env:HOLD = $Hold

Push-Location $PSScriptRoot
try {
    docker compose run --rm k6
    $exitCode = $LASTEXITCODE
}
finally {
    Pop-Location
    foreach ($name in $names) {
        [Environment]::SetEnvironmentVariable($name, $previous[$name], "Process")
    }
}

exit $exitCode
