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

use crate::{
    cargo::run_cargo,
    error::{wrap_error, Error},
    fs::read_files_recursively,
    git::git_toplevel,
};
use std::{
    fs::{create_dir_all, remove_dir_all, remove_file},
    path::Path,
    process::Command,
};

pub(crate) fn run() -> Result<(), Error> {
    let root_dir = git_toplevel()?;
    let coverage_dir = root_dir.join("coverage");

    create_coverage_dir(&coverage_dir)?;
    execute_tests_with_coverage(&root_dir)?;
    execute_grcov(&root_dir, &coverage_dir, "lcov", "lcov.info")?;
    execute_grcov(&root_dir, &coverage_dir, "html", "html")?;
    cleanup_profraw_files(&root_dir);

    Ok(())
}

fn create_coverage_dir(coverage_dir: &Path) -> Result<(), Error> {
    match remove_dir_all(coverage_dir) {
        Err(e) if e.kind() == std::io::ErrorKind::NotFound => (),
        Err(e) => {
            return Err(wrap_error(
                format!("Error while removing coverage directory '{coverage_dir:?}'."),
                e,
            ))
        }
        Ok(_) => (),
    }
    create_dir_all(coverage_dir).map_err(|e| {
        wrap_error(
            format!("Error while creating coverage directory '{coverage_dir:?}'."),
            e,
        )
    })?;
    eprintln!("Coverage directory '{coverage_dir:?}' created.");
    Ok(())
}

fn execute_tests_with_coverage(root_dir: &Path) -> Result<(), Error> {
    eprintln!("Executing tests with coverage...");
    run_cargo(
        Command::new("cargo")
            .args(["test", "--workspace"])
            .current_dir(root_dir)
            .env("CARGO_INCREMENTAL", "0")
            .env("RUSTFLAGS", "-Cinstrument-coverage")
            .env("LLVM_PROFILE_FILE", "cargo-test-%p-%m.profraw"),
    )
}

fn execute_grcov(
    root_dir: &Path,
    coverage_dir: &Path,
    format: &str,
    output: &str,
) -> Result<(), Error> {
    eprintln!("Producing grcov report");
    let mut proc = Command::new("grcov")
        .arg("crates")
        .args(["--binary-path", "target/debug/deps"])
        .args(["-s", "crates"])
        .args(["--ignore", "xtask/**"])
        .arg("--ignore-not-existing")
        .arg("--branch")
        .args(["--excl-start", r"mod\s+tests\s+\{"])
        .args(["--excl-line", r"\s*#\[\s*derive"])
        .args(["-t", format])
        .arg("-o")
        .arg(coverage_dir.join(output))
        .current_dir(root_dir)
        .spawn()
        .map_err(|e| wrap_error("Failed to start grcov process", e))?;

    let exit_status = proc
        .wait()
        .map_err(|e| wrap_error("Failed to wait for grcov process", e))?;

    if !exit_status.success() {
        return Err(format!(
            "`grcov` process failed with exit code {:?}",
            exit_status.code()
        )
        .into());
    }

    Ok(())
}

fn cleanup_profraw_files(root_dir: &Path) {
    read_files_recursively(root_dir)
        .filter_map(|entry| entry.ok())
        .filter(|entry| {
            entry.file_type().is_file()
                && entry
                    .path()
                    .extension()
                    .map(|ext| ext.eq("profraw"))
                    .unwrap_or_default()
        })
        .for_each(|entry| {
            if let Err(e) = remove_file(entry.path()) {
                eprintln!("Failed to delete profraw file `{:?}: {}", entry.path(), e);
            }
        });
}
