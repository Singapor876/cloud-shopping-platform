param(
    [string]$NacosUrl = "http://localhost:8848/nacos",
    [string]$Group = "DEFAULT_GROUP"
)

$ErrorActionPreference = "Stop"
$configDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$files = @(
    "application-dev.yml",
    "cloud-gateway-dev.yml",
    "cloud-system-dev.yml",
    "cloud-file-dev.yml",
    "cloud-cms-dev.yml",
    "cloud-ums-dev.yml",
    "cloud-oms-dev.yml",
    "cloud-gateway-sentinel.json"
)

foreach ($name in $files) {
    $content = Get-Content -LiteralPath (Join-Path $configDirectory $name) -Raw
    $type = if ($name.EndsWith(".json")) { "json" } else { "yaml" }
    $result = Invoke-RestMethod -Method Post -Uri "$NacosUrl/v1/cs/configs" -Body @{
        dataId  = $name
        group   = $Group
        type    = $type
        content = $content
    }
    if ($result -ne $true) {
        throw "导入失败：$name"
    }
    Write-Host "已导入：$name"
}

