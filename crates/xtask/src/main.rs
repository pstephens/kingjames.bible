use clap::{Parser, Subcommand};

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
}

fn main() {
    let cli = Cli::parse();

    let result = match cli.cmd {
        Commands::Coverage => commands::coverage::run(),
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
