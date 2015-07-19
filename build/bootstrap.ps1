$root = git rev-parse --show-toplevel

function DownloadFile($relativePath, $url)
{
    Write-Verbose "Checking dependency $relativePath"

    $fqPath = join-path $root $relativePath
    if(-not (Test-Path $fqPath))
    {
        Write-Verbose "Downloading dependency $relativePath"
        $fqDir = Split-Path $fqPath
        if(-not (Test-Path $fqDir -PathType Container))
        {
            New-Item $fqDir -ItemType Container | Out-Null
        }

        Invoke-WebRequest $url -OutFile $fqPath
    }
}

DownloadFile libs/cljs.jar https://github.com/clojure/clojurescript/releases/download/r3308/cljs.jar
