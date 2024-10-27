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
use std::process::Command;

pub fn run_cargo(cmd: &mut Command) -> Result<(), Error> {
    let mut child = cmd
        .spawn()
        .map_err(|e| wrap_error("Failed to start cargo process", e))?;

    let exit_status = child
        .wait()
        .map_err(|e| wrap_error("Failed to wait for cargo process", e))?;

    if !exit_status.success() {
        return Err(format!(
            "`cargo` process failed with exit code {:?}",
            exit_status.code()
        )
        .into());
    }

    Ok(())
}
