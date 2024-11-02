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
