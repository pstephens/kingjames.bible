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

use crate::util::{InputOpts, OutputOpts};
use clap::{Args, ValueEnum};
use kingjames_bible_ingestion::parsers::staggs;
use serde_json::to_writer_pretty;
use std::{fs::File, io::BufWriter};

#[derive(Debug, Clone, ValueEnum)]
pub enum ParserCommands {
    /// Normalize the bible text curated by Brandon Staggs
    Staggs,
}

#[derive(Args, Debug)]
pub struct NormalizerOpts {
    #[clap(flatten)]
    pub input: InputOpts,

    #[clap(flatten)]
    pub output: OutputOpts,

    #[clap(long)]
    pub parser: ParserCommands,
}

pub fn run(opts: &NormalizerOpts) {
    let bible = match opts.parser {
        ParserCommands::Staggs => staggs::parse(&opts.input.input).unwrap(),
    };

    let writer = BufWriter::new(File::create(&opts.output.output).unwrap());
    to_writer_pretty(writer, &bible).unwrap()
}
