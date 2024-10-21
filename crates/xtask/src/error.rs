use std::borrow::Cow;

pub(crate) type Error = Box<dyn std::error::Error + Send + Sync>;

pub(crate) fn wrap_error(msg: impl Into<Cow<'static, str>>, inner: impl Into<Error>) -> Error {
    WrappedError {
        msg: msg.into(),
        inner: inner.into(),
    }
    .into()
}

#[derive(Debug)]
pub struct WrappedError {
    pub msg: Cow<'static, str>,
    pub inner: Error,
}

impl std::error::Error for WrappedError {}

impl std::fmt::Display for WrappedError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}: caused by {}", self.msg, self.inner)
    }
}
