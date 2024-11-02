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

use crate::model::{Bible, BookName, Chapter};
use std::num::NonZeroU8;

#[derive(Debug, Eq, PartialEq)]
pub enum ContentType {
    Intro,
    Verse(NonZeroU8),
    Postscript,
}

#[derive(Debug, Eq, PartialEq)]
pub struct Content<'a> {
    pub book: BookName,
    pub chapter_num: NonZeroU8,
    pub reference: ContentType,
    pub content: &'a str,
}

pub enum ContentState<'a> {
    Intro(&'a str),
    Verse(usize),
    Postscript(&'a str),
}

pub struct ContentIter<'a> {
    bible: &'a Bible,
    book_index: usize,
    chapter_index: usize,
    content_state: ContentState<'a>,
}

impl<'a> ContentIter<'a> {
    fn advance_content(&mut self) {
        match self.content_state {
            ContentState::Intro(_) => {
                self.content_state = ContentState::Verse(0);
            }
            ContentState::Verse(index) => {
                let chapter = &self.bible.books[self.book_index].chapters[self.chapter_index];
                let index = index + 1;
                if index < chapter.verses.len() {
                    self.content_state = ContentState::Verse(index);
                } else if let Some(ref content) = chapter.postscript {
                    self.content_state = ContentState::Postscript(content.as_str());
                } else {
                    self.advance_chapter();
                }
            }
            ContentState::Postscript(_) => {
                self.advance_chapter();
            }
        }
    }

    fn advance_chapter(&mut self) {
        let index = self.chapter_index + 1;
        if index < self.bible.books[self.book_index].chapters.len() {
            self.chapter_index = index;
            self.content_state =
                Self::get_initial_content_state(&self.bible.books[self.book_index].chapters[index]);
        } else {
            self.advance_book();
        }
    }

    fn advance_book(&mut self) {
        let index = self.book_index + 1;
        self.book_index = index;
        self.chapter_index = 0;
        if index < self.bible.books.len() {
            self.content_state =
                Self::get_initial_content_state(&self.bible.books[index].chapters[0]);
        }
    }

    fn get_initial_content_state(chapter: &'a Chapter) -> ContentState<'a> {
        if let Some(ref content) = chapter.intro {
            ContentState::Intro(content.as_str())
        } else {
            ContentState::Verse(0)
        }
    }

    fn eof(&self) -> bool {
        self.book_index >= self.bible.books.len()
    }
}

impl<'a> Iterator for ContentIter<'a> {
    type Item = Content<'a>;

    fn next(&mut self) -> Option<Self::Item> {
        if self.eof() {
            return None;
        }

        // calc next
        let book = &self.bible.books[self.book_index];
        let chapter = &book.chapters[self.chapter_index];
        let (reference, content) = match self.content_state {
            ContentState::Intro(content) => (ContentType::Intro, content),
            ContentState::Verse(index) => (
                ContentType::Verse(chapter.verses[index].verse_num),
                chapter.verses[index].content.as_str(),
            ),
            ContentState::Postscript(content) => (ContentType::Postscript, content),
        };

        // advance
        self.advance_content();

        Some(Content {
            book: book.book,
            chapter_num: chapter.chapter_num,
            reference,
            content,
        })
    }
}

impl Bible {
    pub fn content_iter(&self) -> ContentIter<'_> {
        assert!(self.books[0].chapters[0].intro.is_none());
        ContentIter {
            bible: self,
            book_index: 0,
            chapter_index: 0,
            content_state: ContentIter::get_initial_content_state(&self.books[0].chapters[0]),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::{Book, Verse};

    #[test]
    fn test_content_iter() {
        let bible = Bible {
            books: vec![
                Book {
                    book: BookName::Genesis,
                    chapters: vec![
                        Chapter {
                            chapter_num: NonZeroU8::new(1).unwrap(),
                            intro: None,
                            verses: vec![
                                Verse {
                                    verse_num: NonZeroU8::new(1).unwrap(),
                                    content: "In the beginning God created the heaven and the earth.".to_string(),
                                },
                                Verse {
                                    verse_num: NonZeroU8::new(2).unwrap(),
                                    content: "And the earth was without form, and void; and darkness [was] upon the face of the deep. And the Spirit of God moved upon the face of the waters.".to_string(),
                                }
                            ],
                            postscript: None,
                        },
                    ],
                },
                Book {
                    book: BookName::Psalms,
                    chapters: vec![
                        Chapter {
                            chapter_num: NonZeroU8::new(2).unwrap(),
                            intro: None,
                            verses: vec![
                                Verse {
                                    verse_num: NonZeroU8::new(1).unwrap(),
                                    content: "Why do the heathen rage, and the people imagine a vain thing?".to_string(),
                                },
                            ],
                            postscript: None,
                        },
                        Chapter {
                            chapter_num: NonZeroU8::new(3).unwrap(),
                            intro: Some("A Psalm of David, when he fled from Absalom his son.".to_string()),
                            verses: vec![
                                Verse {
                                    verse_num: NonZeroU8::new(1).unwrap(),
                                    content: "LORD, how are they increased that trouble me! many [are] they that rise up against me.".to_string(),
                                },
                            ],
                            postscript: None,
                        },
                    ],
                },
                Book {
                    book: BookName::Romans,
                    chapters: vec![
                        Chapter {
                            chapter_num: NonZeroU8::new(16).unwrap(),
                            intro: None,
                            verses: vec![
                                Verse {
                                    verse_num: NonZeroU8::new(1).unwrap(),
                                    content: "I commend unto you Phebe our sister, which is a servant of the church which is at Cenchrea:".to_string(),
                                }
                            ],
                            postscript: Some("Written to the Romans from Corinthus, [and sent] by Phebe servant of the church at Cenchrea.".to_string()),
                        }
                    ]
                }
            ]
        };

        // act
        let content: Vec<_> = bible.content_iter().collect();

        // assert
        assert_eq!(content, vec![
            Content {
                book: BookName::Genesis,
                chapter_num: NonZeroU8::new(1).unwrap(),
                reference: ContentType::Verse(NonZeroU8::new(1).unwrap()),
                content: "In the beginning God created the heaven and the earth."
            },
            Content {
                book: BookName::Genesis,
                chapter_num: NonZeroU8::new(1).unwrap(),
                reference: ContentType::Verse(NonZeroU8::new(2).unwrap()),
                content: "And the earth was without form, and void; and darkness [was] upon the face of the deep. And the Spirit of God moved upon the face of the waters."
            },
            Content {
                book: BookName::Psalms,
                chapter_num: NonZeroU8::new(2).unwrap(),
                reference: ContentType::Verse(NonZeroU8::new(1).unwrap()),
                content: "Why do the heathen rage, and the people imagine a vain thing?"
            },
            Content {
                book: BookName::Psalms,
                chapter_num: NonZeroU8::new(3).unwrap(),
                reference: ContentType::Intro,
                content: "A Psalm of David, when he fled from Absalom his son."
            },
            Content {
                book: BookName::Psalms,
                chapter_num: NonZeroU8::new(3).unwrap(),
                reference: ContentType::Verse(NonZeroU8::new(1).unwrap()),
                content: "LORD, how are they increased that trouble me! many [are] they that rise up against me."
            },
            Content {
                book: BookName::Romans,
                chapter_num: NonZeroU8::new(16).unwrap(),
                reference: ContentType::Verse(NonZeroU8::new(1).unwrap()),
                content: "I commend unto you Phebe our sister, which is a servant of the church which is at Cenchrea:"
            },
            Content {
                book: BookName::Romans,
                chapter_num: NonZeroU8::new(16).unwrap(),
                reference: ContentType::Postscript,
                content: "Written to the Romans from Corinthus, [and sent] by Phebe servant of the church at Cenchrea."
            },
        ]);
    }
}
