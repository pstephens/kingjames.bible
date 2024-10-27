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
