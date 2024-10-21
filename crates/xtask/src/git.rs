use crate::error::{wrap_error, Error};
use std::{path::PathBuf, process::Command};

pub fn git_toplevel() -> Result<PathBuf, Error> {
    // git rev-parse --show-toplevel
    let output = Command::new("git")
        .args(&["rev-parse", "--show-toplevel"])
        .output()
        .map_err(|e| wrap_error("Failed while executing `git rev-parse --show-toplevel`", e))?;

    if !output.status.success() {
        return Err(format!(
            "Failed to execute `git rev-parse --show-toplevel` with exit code {}\n{}",
            output.status,
            String::from_utf8_lossy(&output.stderr)
        )
        .into());
    }

    let mut stdout = String::from_utf8(output.stdout)
        .map_err(|e| wrap_error("Failed to convert output to utf8", e))?;

    // replace '/' with '\\' on Windows
    if std::path::MAIN_SEPARATOR_STR == r"\" {
        stdout = stdout.replace("/", r"\");
    }

    Ok(PathBuf::from(stdout.trim()))
}
