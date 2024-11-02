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

use std::str::CharIndices;

#[derive(Debug, PartialEq, Eq, Hash)]
pub struct Token<'a>(pub &'a str);

const PEEK_SIZE: usize = 2;

pub struct Tokenizer<'a> {
    s: &'a str,
    peek_pos: usize,
    peek_len: usize,
    peek: [(usize, char); PEEK_SIZE],
    iter: CharIndices<'a>,
}

impl<'a> Tokenizer<'a> {
    pub fn new(s: &'a str) -> Self {
        let iter = s.char_indices();
        Self {
            s,
            iter,
            peek_pos: 0,
            peek_len: 0,
            peek: Default::default(),
        }
    }

    fn fill(&mut self) {
        while self.peek_len < PEEK_SIZE {
            let Some(ch) = self.iter.next() else {
                return;
            };
            self.peek[(self.peek_pos + self.peek_len) % PEEK_SIZE] = ch;
            self.peek_len += 1;
        }
    }

    fn peek<const N: usize>(&self) -> Option<(usize, char)> {
        if self.peek_len > N {
            Some(self.peek[(self.peek_pos + N) % PEEK_SIZE])
        } else {
            None
        }
    }

    fn advance(&mut self) {
        if self.peek_len > 0 {
            self.peek_pos = (self.peek_pos + 1) % PEEK_SIZE;
            self.peek_len -= 1;
        }
    }
}

impl<'a> Iterator for Tokenizer<'a> {
    type Item = Token<'a>;

    fn next(&mut self) -> Option<Self::Item> {
        self.fill();
        let (first, first_ch) = self.peek::<0>()?;

        // for words we continue to accumulate characters until we find a non word
        // for punctuation and whitespace we return the single char token
        // hyphens are a special case:
        //   if a hyphyen is in between two word chars then it is part of the word
        //   otherwise it is treated as punctuation
        match classify(first_ch) {
            TokenKind::Hyphen | TokenKind::PunctuationOrWhitespace => {
                self.advance();
                if let Some((next_pos, _)) = self.peek::<0>() {
                    return Some(Token(&self.s[first..next_pos]));
                } else {
                    return Some(Token(&self.s[first..]));
                }
            }
            TokenKind::Word => loop {
                self.advance();
                self.fill();
                let Some((next_pos, next_ch)) = self.peek::<0>() else {
                    return Some(Token(&self.s[first..]));
                };
                match classify(next_ch) {
                    TokenKind::PunctuationOrWhitespace => {
                        return Some(Token(&self.s[first..next_pos]));
                    }
                    TokenKind::Word => continue,
                    TokenKind::Hyphen => {
                        let Some((_, second_ch)) = self.peek::<1>() else {
                            return Some(Token(&self.s[first..next_pos]));
                        };
                        match classify(second_ch) {
                            TokenKind::Word => continue,
                            TokenKind::Hyphen | TokenKind::PunctuationOrWhitespace => {
                                return Some(Token(&self.s[first..next_pos]));
                            }
                        }
                    }
                }
            },
        }
    }
}

#[derive(Debug, PartialEq, Eq)]
enum TokenKind {
    Word,
    Hyphen,
    PunctuationOrWhitespace,
}

/// Classify the characters that are in the King James bible text.
/// Panic on any other character.
fn classify(ch: char) -> TokenKind {
    match ch {
        'a'..='z' | 'A'..='Z' | '\'' => TokenKind::Word,
        ' ' | '.' | ',' | ';' | ':' | '!' | '(' | ')' | '[' | ']' | '?' => {
            TokenKind::PunctuationOrWhitespace
        }
        '-' => TokenKind::Hyphen,
        _ => panic!("Unexpected character: {ch}"),
    }
}

// TODO: brackets need to be separated from other punctuation strings as they'll be needed for rendering
// TODO: hyphens can be puncation or compound word depending on usage. Fix logic.

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_classifier_word_char() {
        assert_eq!(classify('a'), TokenKind::Word);
        assert_eq!(classify('z'), TokenKind::Word);
        assert_eq!(classify('A'), TokenKind::Word);
        assert_eq!(classify('Z'), TokenKind::Word);
        assert_eq!(classify('\''), TokenKind::Word);
    }

    #[test]
    fn test_classifier_punctuation() {
        assert_eq!(classify(' '), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify('.'), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify(','), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify(';'), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify(':'), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify('!'), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify('('), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify(')'), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify('['), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify(']'), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify('?'), TokenKind::PunctuationOrWhitespace);
        assert_eq!(classify('-'), TokenKind::Hyphen);
    }

    #[test]
    #[should_panic]
    fn test_classifier_unexpected_char() {
        classify('$');
    }

    #[test]
    fn test_empty_string() {
        let tokenizer = Tokenizer::new("");
        let tokens: Vec<Token> = tokenizer.collect();
        assert_eq!(tokens, vec![]);
    }

    #[test]
    fn test_tokenizer() {
        let tokenizer = Tokenizer::new("Jesus wept.");
        let tokens: Vec<Token> = tokenizer.collect();
        assert_eq!(
            tokens,
            vec![Token("Jesus"), Token(" "), Token("wept"), Token("."),]
        );
    }

    #[test]
    fn test_brackets() {
        let tokenizer = Tokenizer::new(
            "([And] this taxing was first made when Cyrenius was governor of Syria.)",
        );
        let tokens: Vec<Token> = tokenizer.collect();
        assert_eq!(
            tokens,
            vec![
                Token("("),
                Token("["),
                Token("And"),
                Token("]"),
                Token(" "),
                Token("this"),
                Token(" "),
                Token("taxing"),
                Token(" "),
                Token("was"),
                Token(" "),
                Token("first"),
                Token(" "),
                Token("made"),
                Token(" "),
                Token("when"),
                Token(" "),
                Token("Cyrenius"),
                Token(" "),
                Token("was"),
                Token(" "),
                Token("governor"),
                Token(" "),
                Token("of"),
                Token(" "),
                Token("Syria"),
                Token("."),
                Token(")")
            ]
        );
    }

    #[test]
    fn test_hyphen_word() {
        let tokenizer =
            Tokenizer::new("And he erected there an altar, and called it Elelohe-Israel.");
        let tokens: Vec<Token> = tokenizer.collect();
        assert_eq!(
            tokens,
            vec![
                Token("And"),
                Token(" "),
                Token("he"),
                Token(" "),
                Token("erected"),
                Token(" "),
                Token("there"),
                Token(" "),
                Token("an"),
                Token(" "),
                Token("altar"),
                Token(","),
                Token(" "),
                Token("and"),
                Token(" "),
                Token("called"),
                Token(" "),
                Token("it"),
                Token(" "),
                Token("Elelohe-Israel"),
                Token("."),
            ]
        );
    }

    #[test]
    fn test_hyphen_punctuation() {
        let tokenizer = Tokenizer::new("Yet now, if thou wilt forgive their sin--; and if not, blot me, I pray thee, out of thy book which thou hast written.");
        let tokens: Vec<Token> = tokenizer.collect();
        assert_eq!(
            tokens,
            vec![
                Token("Yet"),
                Token(" "),
                Token("now"),
                Token(","),
                Token(" "),
                Token("if"),
                Token(" "),
                Token("thou"),
                Token(" "),
                Token("wilt"),
                Token(" "),
                Token("forgive"),
                Token(" "),
                Token("their"),
                Token(" "),
                Token("sin"),
                Token("-"),
                Token("-"),
                Token(";"),
                Token(" "),
                Token("and"),
                Token(" "),
                Token("if"),
                Token(" "),
                Token("not"),
                Token(","),
                Token(" "),
                Token("blot"),
                Token(" "),
                Token("me"),
                Token(","),
                Token(" "),
                Token("I"),
                Token(" "),
                Token("pray"),
                Token(" "),
                Token("thee"),
                Token(","),
                Token(" "),
                Token("out"),
                Token(" "),
                Token("of"),
                Token(" "),
                Token("thy"),
                Token(" "),
                Token("book"),
                Token(" "),
                Token("which"),
                Token(" "),
                Token("thou"),
                Token(" "),
                Token("hast"),
                Token(" "),
                Token("written"),
                Token("."),
            ]
        );
    }

    #[test]
    fn test_word_at_end() {
        let tokenizer = Tokenizer::new(
            "So Gad came to David, and said unto him, Thus saith the LORD, Choose thee",
        );
        let tokens: Vec<Token> = tokenizer.collect();
        assert_eq!(
            tokens,
            vec![
                Token("So"),
                Token(" "),
                Token("Gad"),
                Token(" "),
                Token("came"),
                Token(" "),
                Token("to"),
                Token(" "),
                Token("David"),
                Token(","),
                Token(" "),
                Token("and"),
                Token(" "),
                Token("said"),
                Token(" "),
                Token("unto"),
                Token(" "),
                Token("him"),
                Token(","),
                Token(" "),
                Token("Thus"),
                Token(" "),
                Token("saith"),
                Token(" "),
                Token("the"),
                Token(" "),
                Token("LORD"),
                Token(","),
                Token(" "),
                Token("Choose"),
                Token(" "),
                Token("thee"),
            ]
        );
    }

    #[test]
    fn test_hyphen_at_end() {
        let tokenizer = Tokenizer::new("No actual verse representing a hyphen at end-");
        let tokens: Vec<Token> = tokenizer.collect();
        assert_eq!(
            tokens,
            vec![
                Token("No"),
                Token(" "),
                Token("actual"),
                Token(" "),
                Token("verse"),
                Token(" "),
                Token("representing"),
                Token(" "),
                Token("a"),
                Token(" "),
                Token("hyphen"),
                Token(" "),
                Token("at"),
                Token(" "),
                Token("end"),
                Token("-"),
            ]
        );
    }
}
