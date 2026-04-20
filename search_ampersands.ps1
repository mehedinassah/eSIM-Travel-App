$files = Get-ChildItem "app/src/main/res/layout" -Filter "*.xml"
$results = @()
foreach ($file in $files) {
    $lines = Get-Content $file.FullName
    $lineNum = 0
    foreach ($line in $lines) {
        $lineNum++
        # Match & not followed by valid entity references
        if ($line -match '&(?!(?:amp|quot|apos|lt|gt|#\d+|#x[0-9a-fA-F]+);)') {
            $results += "$($file.Name):$lineNum"
            $results += $line
        }
    }
}
if ($results.Count -gt 0) {
    $results
} else {
    Write-Output "No unescaped ampersands found"
}
