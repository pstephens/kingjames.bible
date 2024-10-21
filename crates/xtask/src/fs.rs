use std::{
    collections::VecDeque,
    fs::{read_dir, FileType},
    path::{Path, PathBuf},
};

use crate::error::Error;

/// Read files recursively
pub fn read_files_recursively(path: &Path) -> impl Iterator<Item = Result<DirEntry, Error>> {
    DirReader::new(path)
}

pub struct DirReader {
    stack: VecDeque<Result<DirEntry, Error>>,
}

impl DirReader {
    fn new(initial: &Path) -> Self {
        let stack = VecDeque::from([Self::initial(initial)]);
        Self { stack }
    }

    fn initial(initial: &Path) -> Result<DirEntry, Error> {
        let path = PathBuf::from(initial);
        let metadata = path.metadata()?;
        Ok(DirEntry {
            path,
            file_type: metadata.file_type(),
        })
    }

    fn push_dir(&mut self, path: &Path) {
        let dir = match read_dir(path) {
            Ok(dir) => dir,
            Err(e) => {
                self.stack.push_back(Err(e.into()));
                return;
            }
        };

        for entry in dir {
            match entry {
                Ok(entry) => match entry.metadata() {
                    Ok(metadata) => self.stack.push_back(Ok(DirEntry {
                        path: entry.path(),
                        file_type: metadata.file_type(),
                    })),
                    Err(e) => {
                        self.stack.push_back(Err(e.into()));
                    }
                },
                Err(e) => {
                    self.stack.push_back(Err(e.into()));
                }
            }
        }
    }
}

impl Iterator for DirReader {
    type Item = Result<DirEntry, Error>;

    fn next(&mut self) -> Option<Self::Item> {
        let entry = match self.stack.pop_front() {
            Some(Ok(entry)) => entry,
            Some(Err(err)) => return Some(Err(err)),
            None => return None,
        };
        if entry.file_type().is_dir() {
            self.push_dir(entry.path());
        }

        Some(Ok(entry))
    }
}

#[derive(Debug)]
pub struct DirEntry {
    file_type: FileType,
    path: PathBuf,
}

impl DirEntry {
    pub fn file_type(&self) -> &FileType {
        &self.file_type
    }

    pub fn path(&self) -> &Path {
        &self.path
    }
}
