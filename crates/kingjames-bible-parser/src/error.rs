use std::borrow::Cow;

#[derive(Debug)]
pub enum ParserError {
    Validation(Cow<'static, str>),
    Io(Error),
}

impl ParserError {
    pub fn validation(msg: impl Into<Cow<'static, str>>) -> ParserError {
        ParserError::Validation(msg.into())
    }

    pub fn io(e: impl Into<Error>) -> ParserError {
        ParserError::Io(e.into())
    }
}

impl std::fmt::Display for ParserError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            ParserError::Validation(msg) => write!(f, "Validation error: {msg}"),
            ParserError::Io(e) => write!(f, "I/O error: {e}"),
        }
    }
}

impl std::error::Error for ParserError {}

pub type Error = Box<dyn std::error::Error + Send + Sync>;
