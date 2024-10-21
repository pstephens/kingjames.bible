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
