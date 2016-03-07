param(
    $dir = "$psscriptroot\..\out\static",
    $bucket = "everlastingbible.com"
)

$existing = Get-S3Object -bucketname $bucket |
    foreach-object `
        -begin { $items = @{} } `
        -process { $items[$_.Key] = $_ } `
        -end { $items }

$new = Get-ChildItem $dir |
    foreach-object `
        -begin { $items = @{} } `
        -process { $items[$_.Name] = $_ } `
        -end { $items }

$keysToDelete = @($existing.Keys | where { -not $new.ContainsKey($_) })
$keysToAdd = @($new.keys | where { -not $existing.ContainsKey($_) })
$keysToUpdate = @($new.keys | where { $existing.ContainsKey($_) })

function ContentTypeFromKey($key)
{
    switch -regex ($key) {
        "\.html$" { "text/html" }
        "\.css$" { "text/css" }
        "^[^.]+$" { "text/html" }
        default { throw "Invalid file key: $key" }
    }
}

function WriteFile($key)
{
    Write-S3Object -bucketname $bucket `
        -file ($new[$key].FullName) `
        -contenttype (ContentTypeFromKey $key) `
        -headercollection @{
            "Cache-Control" = "max-age=600"
        } `
        -key $key `
        -publicreadonly `
        -standardstorage
}

$keysToAdd |
    sort-object |
    foreach-object {
        Write-Host "Adding $_"
        WriteFile $_
    }

$keysToUpdate |
    sort-object |
    where {
        $etag = $existing[$_].ETag
        $md5 = "`"$((Get-FileHash ($new[$_].FullName) -algorithm md5).Hash)`""
        $etag -ne $md5
    } |
    foreach-object {
        Write-Host "Updating $_"
        WriteFile $_
    }