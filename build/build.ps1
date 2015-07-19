param(
    $buildClj = "build/normalizer.clj"
)

& (Join-Path $PSScriptRoot bootstrap.ps1)

$root = git rev-parse --show-toplevel

function RunInPath($path, [scriptblock] $runMe)
{
    Push-Location $path
    try {
        & $runMe
    }
    finally {
        Pop-Location
    }
}

RunInPath $root {
    java -cp "libs\cljs.jar;src" clojure.main $buildClj
}