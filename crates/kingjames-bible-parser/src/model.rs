use serde::{Deserialize, Serialize};
use std::num::NonZeroU8;

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq)]
pub struct Verse {
    pub verse_num: NonZeroU8,
    pub content: String,
}

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq)]
pub struct Chapter {
    pub chapter_num: NonZeroU8,
    pub intro: Option<String>,
    pub verses: Vec<Verse>,
    pub postscript: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq)]
pub struct Book {
    pub book: BookName,
    pub chapters: Vec<Chapter>,
}

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq)]
pub struct Bible {
    pub books: Vec<Book>,
}

#[derive(Serialize, Deserialize, Debug, Copy, Clone, Eq, PartialEq, Hash, PartialOrd, Ord)]
#[repr(u8)]
pub enum BookName {
    Genesis,
    Exodus,
    Leviticus,
    Numbers,
    Deuteronomy,
    Joshua,
    Judges,
    Ruth,
    Samuel1,
    Samuel2,
    Kings1,
    Kings2,
    Chronicles1,
    Chronicles2,
    Ezra,
    Nehemiah,
    Esther,
    Job,
    Psalms,
    Proverbs,
    Ecclesiastes,
    SongOfSolomon,
    Isaiah,
    Jeremiah,
    Lamentations,
    Ezekiel,
    Daniel,
    Hosea,
    Joel,
    Amos,
    Obadiah,
    Jonah,
    Micah,
    Nahum,
    Habakkuk,
    Zephaniah,
    Haggai,
    Zechariah,
    Malachi,
    Matthew,
    Mark,
    Luke,
    John,
    Acts,
    Romans,
    Corinthians1,
    Corinthians2,
    Galatians,
    Ephesians,
    Philippians,
    Colossians,
    Thessalonians1,
    Thessalonians2,
    Timothy1,
    Timothy2,
    Titus,
    Philemon,
    Hebrews,
    James,
    Peter1,
    Peter2,
    John1,
    John2,
    John3,
    Jude,
    Revelation,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_book_name_ord() {
        use BookName::*;
        assert!(Genesis < Revelation);
        assert!(Philippians < Colossians);
        assert!(Mark < Luke);
    }
}
