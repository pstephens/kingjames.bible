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

use clap::{Parser, Subcommand};

mod cargo;
mod commands;
mod error;
mod fs;
mod git;

#[derive(Debug, Parser)]
#[command(author, version, about, long_about = None)]
struct Cli {
    #[command(subcommand)]
    cmd: Commands,
}

#[derive(Debug, Subcommand, Clone)]
enum Commands {
    /// Execute unit tests with coverage
    Coverage,
    /// Parse and normalize bible text
    Normalizer,
}

fn main() {
    let cli = Cli::parse();

    let result = match cli.cmd {
        Commands::Coverage => commands::coverage::run(),
        Commands::Normalizer => commands::normalizer::run(),
    };

    match result {
        Ok(_) => {
            std::process::exit(0);
        }
        Err(e) => {
            eprintln!("Failed: {e}");
            std::process::exit(1);
        }
    }
}
