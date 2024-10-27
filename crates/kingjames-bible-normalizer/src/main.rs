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

//! Raw bible text normalizer - converts a raw bible text to a normalized format for ease of
//! ingestion and comparison

use clap::{Args, Parser, Subcommand};
use kingjames_bible_parser::staggs;
use serde_json::to_writer_pretty;
use std::{fs::File, io::BufWriter, path::PathBuf};

fn main() {
    let opts = Opts::parse();

    let bible = match opts.parser {
        ParserCommands::Staggs => staggs::parse(&opts.inout.input).unwrap(),
    };

    let writer = BufWriter::new(File::create(&opts.inout.output).unwrap());
    to_writer_pretty(writer, &bible).unwrap()
}

#[derive(Subcommand, Debug)]
enum ParserCommands {
    /// Normalize the bible text curated by Brandon Staggs
    Staggs,
}

#[derive(Parser, Debug)]
#[command(version, about)]
struct Opts {
    #[command(subcommand)]
    parser: ParserCommands,

    #[command(flatten)]
    inout: InOut,
}

#[derive(Args, Debug)]
struct InOut {
    /// Raw input path
    #[arg(long)]
    input: PathBuf,

    /// Normalized output file path
    #[arg(long)]
    output: PathBuf,
}
