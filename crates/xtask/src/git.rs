/*   Copyright 2024 Peter Stephens. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *    limitations under the License.
 */

use crate::error::{wrap_error, Error};
use std::{path::PathBuf, process::Command};

pub fn git_toplevel() -> Result<PathBuf, Error> {
    // git rev-parse --show-toplevel
    let output = Command::new("git")
        .args(["rev-parse", "--show-toplevel"])
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
