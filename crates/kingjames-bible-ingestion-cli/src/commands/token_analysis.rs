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

use crate::util::InputOpts;
use kingjames_bible_ingestion::{
    io::read_from_file,
    tokenizer::{Token, Tokenizer},
};
use std::collections::HashMap;

struct TokenState {
    count: usize,
}

pub fn run(input: &InputOpts) {
    let bible = read_from_file(&input.input);
    let mut tokens: HashMap<String, TokenState> = Default::default();
    for content in bible.content_iter() {
        for Token(t) in Tokenizer::new(content.content) {
            match tokens.get_mut(t) {
                Some(state) => state.count += 1,
                None => {
                    tokens.insert(t.to_string(), TokenState { count: 1 });
                }
            }
        }
    }

    println!("Token count: {}", tokens.len());
    println!(
        "Token bytes: {}",
        tokens.keys().map(|t| t.len()).sum::<usize>()
    );

    let mut token_list: Vec<_> = tokens.into_iter().collect();
    token_list.sort_by(|(_, a), (_, b)| b.count.cmp(&a.count));
    for (token, state) in token_list.iter().take(127) {
        println!("'{}': {}", token, state.count);
    }
}

// simple encoding scheme:
// 1 byte: 0x00 - 0x7F
// 2 bytes: 0x8000 - 0xFFFF
// 0-127 = 128
// 0-32767 = 32768
// total = 32896
