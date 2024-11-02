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

use crate::{cargo::run_cargo, error::Error, git::git_toplevel};
use std::{ffi::OsStr, process::Command};

/// Opinionated normalizer -- user the staggs parser and writes the output
/// to the kingjames-bible-core crate to be checked in as source. This file
/// will be used by the core crate and embedded in a compact format into
/// the resulting binary.
pub(crate) fn run() -> Result<(), Error> {
    let root_dir = git_toplevel()?;
    let target_dir = root_dir.join("crates/kingjames-bible-core");
    let target_path = target_dir.join("kjv.json");
    let source_path = root_dir.join("kjv-src/www.staggs.pair.com-kjbp/kjv.txt");
    let args: &[&OsStr] = &[
        "run".as_ref(),
        "--release".as_ref(),
        "--bin".as_ref(),
        "kingjames-bible-ingestion-cli".as_ref(),
        "--".as_ref(),
        "normalizer".as_ref(),
        "--input".as_ref(),
        source_path.as_os_str(),
        "--output".as_ref(),
        target_path.as_os_str(),
        "--parser".as_ref(),
        "staggs".as_ref(),
    ];

    eprintln!("Normalizing bible text...\ncargo {args:?}");
    run_cargo(Command::new("cargo").args(args).current_dir(root_dir))
}
