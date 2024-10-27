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

//! Parse King James version bible text originally curated by Brandon Staggs
//! Originally downloaded from www.staggs.pair.com/kjbp

use crate::error::{Error, ParserError};
use crate::model::{Bible, Book, BookName, Chapter, Verse};
use regex::Regex;
use std::collections::hash_map::Entry;
use std::collections::HashMap;
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::num::NonZeroU8;
use std::path::PathBuf;
use std::str::FromStr;

pub fn parse(input: &PathBuf) -> Result<Bible, Error> {
    let input = File::open(input).unwrap();
    parse_lines(
        BufReader::new(input)
            .lines()
            .map(|r| r.map_err(|e| ParserError::io(e).into())),
    )
}

type IntermediateVerses = HashMap<NonZeroU8, String>;
type IntermedateChapters = HashMap<NonZeroU8, (Option<String>, IntermediateVerses, Option<String>)>;
type IntermediateBooks = HashMap<BookName, IntermedateChapters>;

#[derive(Debug, Eq, PartialEq)]
struct IntermediateVerse {
    book: BookName,
    chapter_num: NonZeroU8,
    verse_num: NonZeroU8,
    intro: Option<String>,
    postscript: Option<String>,
    content: String,
}

struct ParseContext {
    book_name_map: HashMap<&'static str, BookName>,
    parts_pattern: Regex,
}

impl ParseContext {
    fn new() -> Self {
        Self {
            book_name_map: book_name_map(),
            parts_pattern: Regex::new(
                concat!(r"^\s*(\w+)\s+(\d+)\:(\d+)\s+(?:<<(.*)>>)?",
                r"\s*(?:ALEPH\. |BETH\. |GIMEL\. |DALETH\. |HE\. |VAU\. |ZAIN\. |CHETH\. |TETH\. |JOD\. |CAPH\. |LAMED\. |MEM\. |NUN\. |SAMECH\. |AIN\. |PE\. |TZADDI\. |KOPH\. |RESH\. |SCHIN\. |TAU\. )?",
                r"\s*([^<]*)(?:\s+<<\[(.*)\]>>\s*)?$"),
            )
            .unwrap(),
        }
    }
}

fn parse_lines(lines: impl Iterator<Item = Result<String, Error>>) -> Result<Bible, Error> {
    let context = ParseContext::new();
    let intermediates: IntermediateBooks = lines
        .filter_map(|line| {
            let line = line.expect("Failed to read input line");
            if line.is_empty() {
                None
            } else {
                Some(line)
            }
        })
        .map(|line| parse_line(&context, &line))
        .try_fold(
            Default::default(),
            |mut acc, v| -> Result<IntermediateBooks, Error> {
                let verse = v?;
                let (intro, verses, postscript) = acc
                    .entry(verse.book)
                    .or_default()
                    .entry(verse.chapter_num)
                    .or_default();
                match verses.entry(verse.verse_num) {
                    Entry::Vacant(x) => x.insert(verse.content),
                    Entry::Occupied(_) => panic!("Duplicate verse detected"),
                };
                combine(intro, verse.intro)?;
                combine(postscript, verse.postscript)?;

                Ok(acc)
            },
        )?;

    Ok(Bible {
        books: finalize_books(intermediates),
    })
}

fn combine(a: &mut Option<String>, b: Option<String>) -> Result<(), Error> {
    match (a, b) {
        (Some(_), Some(_)) => {
            Err(ParserError::validation("`intro` already assigned for Chapter").into())
        }
        (a @ None, Some(b)) => {
            *a = Some(b);
            Ok(())
        }
        (_, None) => Ok(()),
    }
}

fn finalize_books(books: IntermediateBooks) -> Vec<Book> {
    let mut books: Vec<_> = books
        .into_iter()
        .map(|(name, chapters)| Book {
            book: name,
            chapters: finalize_chapters(chapters),
        })
        .collect();
    books.sort_by_key(|b| b.book);
    books
}

fn finalize_chapters(chapters: IntermedateChapters) -> Vec<Chapter> {
    let mut chapters: Vec<_> = chapters
        .into_iter()
        .map(|(num, (intro, verses, postscript))| Chapter {
            chapter_num: num,
            intro,
            verses: finalize_verses(verses),
            postscript,
        })
        .collect();
    chapters.sort_by_key(|ch| ch.chapter_num);
    chapters
}

fn finalize_verses(verses: IntermediateVerses) -> Vec<Verse> {
    let mut verses: Vec<_> = verses
        .into_iter()
        .map(|(num, v)| Verse {
            verse_num: num,
            content: v,
        })
        .collect();
    verses.sort_by_key(|v| v.verse_num);
    verses
}

fn parse_line(context: &ParseContext, line: &str) -> Result<IntermediateVerse, Error> {
    let Some(captures) = context.parts_pattern.captures(line) else {
        println!(
            "Line: {} len {} is_empty {}",
            line,
            line.len(),
            line.is_empty()
        );
        panic!("Failed to match verse pattern: '{line}'");
    };

    let mut it = captures.iter();
    it.next().unwrap(); // skip over the first (whole) match
    let book = *it
        .next()
        .expect("Missing book name part")
        .and_then(|book_name| context.book_name_map.get(book_name.as_str()))
        .expect("Invalid book name");
    let chapter_num = it
        .next()
        .expect("Missing chapter part")
        .and_then(|chapter| NonZeroU8::from_str(chapter.as_str()).ok())
        .expect("Failed to parse chapter number");
    let verse_num = it
        .next()
        .expect("Missing verse num part")
        .and_then(|chapter| NonZeroU8::from_str(chapter.as_str()).ok())
        .expect("Failed to parse verse number");
    let intro = it
        .next()
        .expect("Missing verse intro")
        .map(|intro| intro.as_str().to_string());
    let content = it
        .next()
        .expect("Missing verse content")
        .map(|content| content.as_str().to_string())
        .expect("Missing verse content");
    let postscript = it
        .next()
        .expect("Missing verse postscript")
        .map(|intro| intro.as_str().to_string());

    Ok(IntermediateVerse {
        book,
        chapter_num,
        verse_num,
        intro,
        postscript,
        content,
    })
}

fn book_name_map() -> HashMap<&'static str, BookName> {
    use BookName::*;
    HashMap::from([
        ("Ge", Genesis),
        ("Ex", Exodus),
        ("Le", Leviticus),
        ("Nu", Numbers),
        ("De", Deuteronomy),
        ("Jos", Joshua),
        ("Jg", Judges),
        ("Ru", Ruth),
        ("1Sa", Samuel1),
        ("2Sa", Samuel2),
        ("1Ki", Kings1),
        ("2Ki", Kings2),
        ("1Ch", Chronicles1),
        ("2Ch", Chronicles2),
        ("Ezr", Ezra),
        ("Ne", Nehemiah),
        ("Es", Esther),
        ("Job", Job),
        ("Ps", Psalms),
        ("Pr", Proverbs),
        ("Ec", Ecclesiastes),
        ("So", SongOfSolomon),
        ("Isa", Isaiah),
        ("Jer", Jeremiah),
        ("La", Lamentations),
        ("Eze", Ezekiel),
        ("Da", Daniel),
        ("Ho", Hosea),
        ("Joe", Joel),
        ("Am", Amos),
        ("Ob", Obadiah),
        ("Jon", Jonah),
        ("Mic", Micah),
        ("Na", Nahum),
        ("Hab", Habakkuk),
        ("Zep", Zephaniah),
        ("Hag", Haggai),
        ("Zec", Zechariah),
        ("Mal", Malachi),
        ("Mt", Matthew),
        ("Mr", Mark),
        ("Lu", Luke),
        ("Joh", John),
        ("Ac", Acts),
        ("Ro", Romans),
        ("1Co", Corinthians1),
        ("2Co", Corinthians2),
        ("Ga", Galatians),
        ("Eph", Ephesians),
        ("Php", Philippians),
        ("Col", Colossians),
        ("1Th", Thessalonians1),
        ("2Th", Thessalonians2),
        ("1Ti", Timothy1),
        ("2Ti", Timothy2),
        ("Tit", Titus),
        ("Phm", Philemon),
        ("Heb", Hebrews),
        ("Jas", James),
        ("1Pe", Peter1),
        ("2Pe", Peter2),
        ("1Jo", John1),
        ("2Jo", John2),
        ("3Jo", John3),
        ("Jude", Jude),
        ("Re", Revelation),
    ])
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::{Chapter, Verse};

    #[test]
    fn test_gen_1_1() {
        let context = ParseContext::new();
        let line = " Ge 1:1 In the beginning God created the heaven and the earth.";
        let parsed = parse_line(&context, line);
        assert_eq!(
            parsed.unwrap(),
            IntermediateVerse {
                book: BookName::Genesis,
                chapter_num: 1.try_into().unwrap(),
                verse_num: 1.try_into().unwrap(),
                intro: None,
                postscript: None,
                content: "In the beginning God created the heaven and the earth.".to_string()
            }
        );
    }

    #[test]
    fn test_rev_22_21() {
        let context = ParseContext::new();
        let line = " Re 22:21 The grace of our Lord Jesus Christ [be] with you all. Amen.";
        let parsed = parse_line(&context, line);
        assert_eq!(
            parsed.unwrap(),
            IntermediateVerse {
                book: BookName::Revelation,
                chapter_num: 22.try_into().unwrap(),
                verse_num: 21.try_into().unwrap(),
                intro: None,
                postscript: None,
                content: "The grace of our Lord Jesus Christ [be] with you all. Amen.".to_string()
            }
        );
    }

    #[test]
    fn test_psalm_119_9_should_strip_out_hebrew_letter_prefix() {
        let context = ParseContext::new();
        let line = " Ps 119:9 BETH. Wherewithal shall a young man cleanse his way? by taking heed [thereto] according to thy word.";
        let parsed = parse_line(&context, line);
        assert_eq!(
            parsed.unwrap(),
            IntermediateVerse {
                book: BookName::Psalms,
                chapter_num: 119.try_into().unwrap(),
                verse_num: 9.try_into().unwrap(),
                intro: None,
                postscript: None,
                content: "Wherewithal shall a young man cleanse his way? by taking heed [thereto] according to thy word.".to_string()
            }
        );
    }

    #[test]
    fn test_rev_17_5() {
        let context = ParseContext::new();
        let line = " Re 17:5 And upon her forehead [was] a name written, MYSTERY, BABYLON THE GREAT, THE MOTHER OF HARLOTS AND ABOMINATIONS OF THE EARTH.";
        let parsed = parse_line(&context, line);
        assert_eq!(
            parsed.unwrap(),
            IntermediateVerse {
                book: BookName::Revelation,
                chapter_num: 17.try_into().unwrap(),
                verse_num: 5.try_into().unwrap(),
                intro: None,
                postscript: None,
                content: "And upon her forehead [was] a name written, MYSTERY, BABYLON THE GREAT, THE MOTHER OF HARLOTS AND ABOMINATIONS OF THE EARTH.".to_string()
            }
        );
    }

    #[test]
    fn test_psalm_3_1_should_extract_intro() {
        let context = ParseContext::new();
        let line = " Ps 3:1 <<A Psalm of David, when he fled from Absalom his son.>> LORD, how are they increased that trouble me! many [are] they that rise up against me.";
        let parsed = parse_line(&context, line);
        assert_eq!(
            parsed.unwrap(),
            IntermediateVerse {
                book: BookName::Psalms,
                chapter_num: 3.try_into().unwrap(),
                verse_num: 1.try_into().unwrap(),
                intro: Some("A Psalm of David, when he fled from Absalom his son.".to_string()),
                postscript: None,
                content: "LORD, how are they increased that trouble me! many [are] they that rise up against me.".to_string()
            }
        );
    }

    #[test]
    fn test_romans_16_27_should_extract_postscript() {
        let context = ParseContext::new();
        let line = " Ro 16:27 To God only wise, [be] glory through Jesus Christ for ever. Amen. <<[Written to the Romans from Corinthus, [and sent] by Phebe servant of the church at Cenchrea.]>>";
        let parsed = parse_line(&context, line);
        assert_eq!(
            parsed.unwrap(),
            IntermediateVerse {
                book: BookName::Romans,
                chapter_num: 16.try_into().unwrap(),
                verse_num: 27.try_into().unwrap(),
                intro: None,
                postscript: Some("Written to the Romans from Corinthus, [and sent] by Phebe servant of the church at Cenchrea.".to_string()),
                content: "To God only wise, [be] glory through Jesus Christ for ever. Amen.".to_string()
            }
        );
    }

    #[test]
    fn test_parse_lines_gen_1_1() {
        let lines = [" Ge 1:1 In the beginning God created the heaven and the earth."];
        let result = parse_lines(lines.iter().map(|i| Ok(i.to_string())));
        assert_eq!(
            result.unwrap(),
            Bible {
                books: vec![Book {
                    book: BookName::Genesis,
                    chapters: vec![Chapter {
                        chapter_num: 1.try_into().unwrap(),
                        intro: None,
                        verses: vec![Verse {
                            verse_num: 1.try_into().unwrap(),
                            content: "In the beginning God created the heaven and the earth."
                                .to_string()
                        }],
                        postscript: None
                    }]
                }]
            }
        )
    }

    #[test]
    fn test_parse_lines_mark_16_20_luke_1_1() {
        let lines = [
            " Mr 16:20 And they went forth, and preached every where, the Lord working with [them], and confirming the word with signs following. Amen.",
            " Lu 1:1 Forasmuch as many have taken in hand to set forth in order a declaration of those things which are most surely believed among us,",
        ];
        let result = parse_lines(lines.iter().map(|i| Ok(i.to_string())));
        assert_eq!(
            result.unwrap(),
            Bible {
                books: vec![Book {
                    book: BookName::Mark,
                    chapters: vec![Chapter {
                        chapter_num: 16.try_into().unwrap(),
                        intro: None,
                        verses: vec![Verse {
                            verse_num: 20.try_into().unwrap(),
                            content: "And they went forth, and preached every where, the Lord working with [them], and confirming the word with signs following. Amen."
                                .to_string()
                        }],
                        postscript: None
                    }]
                }, Book {
                    book: BookName::Luke,
                    chapters: vec![Chapter {
                        chapter_num: 1.try_into().unwrap(),
                        intro: None,
                        verses: vec![Verse {
                            verse_num: 1.try_into().unwrap(),
                            content: "Forasmuch as many have taken in hand to set forth in order a declaration of those things which are most surely believed among us,".to_string()
                        }],
                        postscript: None
                    }]
                }]
            }
        )
    }
    #[test]
    fn test_parse_lines_mark_16_19_to_20() {
        let lines = [
            " Mr 16:19 So then after the Lord had spoken unto them, he was received up into heaven, and sat on the right hand of God.",
            " Mr 16:20 And they went forth, and preached every where, the Lord working with [them], and confirming the word with signs following. Amen.",
            ];
        let result = parse_lines(lines.iter().map(|i| Ok(i.to_string())));
        assert_eq!(
            result.unwrap(),
            Bible {
                books: vec![Book {
                    book: BookName::Mark,
                    chapters: vec![Chapter {
                        chapter_num: 16.try_into().unwrap(),
                        intro: None,
                        verses: vec![ Verse {
                            verse_num: 19.try_into().unwrap(),
                            content: "So then after the Lord had spoken unto them, he was received up into heaven, and sat on the right hand of God.".to_string()
                        }, Verse {
                            verse_num: 20.try_into().unwrap(),
                            content: "And they went forth, and preached every where, the Lord working with [them], and confirming the word with signs following. Amen."
                                .to_string()
                        }],
                        postscript: None
                    }]
                }]
            }
        )
    }

    #[test]
    fn test_parse_lines_1john_4_21_to_5_1() {
        let lines = [
            " 1Jo 4:21 And this commandment have we from him, That he who loveth God love his brother also.",
            " 1Jo 5:1 Whosoever believeth that Jesus is the Christ is born of God: and every one that loveth him that begat loveth him also that is begotten of him.",
            ];
        let result = parse_lines(lines.iter().map(|i| Ok(i.to_string())));
        assert_eq!(
            result.unwrap(),
            Bible {
                books: vec![Book {
                    book: BookName::John1,
                    chapters: vec![Chapter {
                        chapter_num: 4.try_into().unwrap(),
                        intro: None,
                        verses: vec![ Verse {
                            verse_num: 21.try_into().unwrap(),
                            content: "And this commandment have we from him, That he who loveth God love his brother also.".to_string()
                        }],
                        postscript: None
                    }, Chapter {
                        chapter_num: 5.try_into().unwrap(),
                        intro: None,
                        verses: vec![ Verse {
                            verse_num: 1.try_into().unwrap(),
                            content: "Whosoever believeth that Jesus is the Christ is born of God: and every one that loveth him that begat loveth him also that is begotten of him.".to_string()
                        }],
                        postscript: None
                    }]
                }]
            }
        )
    }

    #[test]
    fn test_parse_lines_should_extract_intro() {
        let lines = [
            " Ps 3:1 <<A Psalm of David, when he fled from Absalom his son.>> LORD, how are they increased that trouble me! many [are] they that rise up against me.",
            " Ps 3:2 Many [there be] which say of my soul, [There is] no help for him in God. Selah.",
            " Ps 3:3 But thou, O LORD, [art] a shield for me; my glory, and the lifter up of mine head.",
        ];
        let result = parse_lines(lines.iter().map(|i| Ok(i.to_string())));
        assert_eq!(
            result.unwrap(),
            Bible {
                books: vec![Book {
                    book:BookName::Psalms,
                    chapters: vec![
                        Chapter {
                            chapter_num: 3.try_into().unwrap(),
                            intro: Some("A Psalm of David, when he fled from Absalom his son.".to_string()),
                            verses: vec![Verse {
                                verse_num: 1.try_into().unwrap(),
                                content: "LORD, how are they increased that trouble me! many [are] they that rise up against me.".to_string()
                            }, Verse {
                                verse_num: 2.try_into().unwrap(),
                                content: "Many [there be] which say of my soul, [There is] no help for him in God. Selah.".to_string()
                            }, Verse {
                                verse_num: 3.try_into().unwrap(),
                                content: "But thou, O LORD, [art] a shield for me; my glory, and the lifter up of mine head.".to_string()
                            }],
                            postscript: None
                        }]
                }]
            }
        );
    }

    #[test]
    fn test_parse_lines_should_extract_postscript() {
        let lines = [
            " Ro 16:25 Now to him that is of power to stablish you according to my gospel, and the preaching of Jesus Christ, according to the revelation of the mystery, which was kept secret since the world began,",
            " Ro 16:26 But now is made manifest, and by the scriptures of the prophets, according to the commandment of the everlasting God, made known to all nations for the obedience of faith:",
            " Ro 16:27 To God only wise, [be] glory through Jesus Christ for ever. Amen. <<[Written to the Romans from Corinthus, [and sent] by Phebe servant of the church at Cenchrea.]>>",
            ];
        let result = parse_lines(lines.iter().map(|i| Ok(i.to_string())));
        assert_eq!(
            result.unwrap(),
            Bible {
                books: vec![Book {
                    book: BookName::Romans,
                    chapters: vec![
                        Chapter {
                            chapter_num: 16.try_into().unwrap(),
                            intro: None,
                            verses: vec![Verse {
                                verse_num: 25.try_into().unwrap(),
                                content: "Now to him that is of power to stablish you according to my gospel, and the preaching of Jesus Christ, according to the revelation of the mystery, which was kept secret since the world began,".to_string()
                            }, Verse {
                                verse_num: 26.try_into().unwrap(),
                                content: "But now is made manifest, and by the scriptures of the prophets, according to the commandment of the everlasting God, made known to all nations for the obedience of faith:".to_string()
                            }, Verse {
                                verse_num: 27.try_into().unwrap(),
                                content: "To God only wise, [be] glory through Jesus Christ for ever. Amen.".to_string()
                            }],
                            postscript: Some("Written to the Romans from Corinthus, [and sent] by Phebe servant of the church at Cenchrea.".to_string())
                        }]
                }]
            }
        );
    }
}
