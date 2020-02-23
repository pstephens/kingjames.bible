const _ = require('lodash');
const cp = require('child_process');
const del = require('del');
const fs = require('fs');
const gulp = require('gulp');
const jasmine = require('jasmine-core').files;
const less = require('less');
const mkdirp = require('mkdirp');
const os = require('os');
const path = require('path');
const puppeteer = require('puppeteer');
const uglify = require('gulp-uglify');

const config = (function parse_commandline() {
    const argv_opts = {
        string: ['config'],
        alias: {config: 'c'},
        default: {'config': 'beta'}
    };
    const argv = require("minimist")(process.argv.slice(2), argv_opts);
    const default_config = {
        bible_src: 'kjv-src/www.staggs.pair.com-kjbp/kjv.txt',
        bible_parser: 'staggs',
        name: argv.config
    };

    let config;
    if (argv.config) {
        config = JSON.parse(fs.readFileSync(path.join("configs", argv.config + ".json")));
    }
    else {
        config = {};
    }

    return _.merge(default_config, config, argv);
})();

const root_dir = __dirname;
const node_exec_path = process.execPath;
const out_bible_dir = path.join(root_dir, 'out/bible');
const build_dir = path.join(root_dir, 'out/' + config.name);
const temp_dir = path.join(root_dir, 'out/temp');
const votd_dir = path.join(build_dir, 'votd');
const test_dir = path.join(build_dir, "test");
const jasmine_dir = path.join(test_dir, 'jasmine');
const temp_votd_dir = path.join(temp_dir, 'votd');
const content_dir = path.join(root_dir, 'src/content');

function output_to(name, stream) {
    return function output_to(buff) {
        _(buff.toString().split(/[\r\n]/))
            .filter(function(line) {
                return !(/^\s*$/.test(line)); })
            .forEach(function(line) {
                stream.write(name + ': ' + line + os.EOL);
            });
    }
}

function spawn(name, command, args, options) {
    args = args || [];
    options = Object.assign({ cwd: root_dir }, options || {});
    let proc = cp.spawn(command, args, options);

    let p = new Promise(
        function (resolve, reject) {
            proc.stdout.on('data', output_to(name, process.stdout));
            proc.stderr.on('data', output_to(name, process.stderr));
            proc.on('close', function(code) {
                    if(code) {
                        reject(new Error(name + ': failed with exit code ' + code));
                    }
                    else {
                        resolve();
                    }
                });
            proc.on('error', function(err) {
                reject(err);
            });
        });

    return {
        proc: proc,
        promise: p
    };
}

function exec(name, command, args) {
    return spawn(name, command, args, { shell: true });
}

function biblecli(cmd, args) {
    return spawn('biblecli-' + cmd, node_exec_path,
        _.concat(['biblecli.js', cmd], _.slice(arguments, 1)));
}

function biblecli_task(cmd, args) {
    args = _.slice(arguments).filter(n => n !== null);
    const f = function biblecli_task() {
        return biblecli.apply(null, args).promise;
    };
    f.displayName = "biblecli " + args[0];
    return f;
}

function mkdir_task(dir) {
    return function mkdir_task() {
        return mkdirp(dir);
    }
}

function copy_task(src, dst, cwd) {
    return function copy_task() {
        return gulp.src(src, { cwd: cwd || root_dir }).pipe(gulp.dest(dst));
    }
}

gulp.task('compile_dbg',
    function exec_lein() {
        return exec('lein', 'lein', ['cljsbuild', 'auto', 'dbg']).promise;
    });

gulp.task('bible_resources_dir', mkdir_task(out_bible_dir));

gulp.task('bible_resources',
    gulp.series(
        'bible_resources_dir',
        biblecli_task('prepare', 'out/bible')));

gulp.task('run_node_tests', biblecli_task('unittest'));

gulp.task('run_browser_tests', async function server() {
    // launch the web server
    const server = biblecli('serve');
    const stopWebServer = () => {
        console.log('Stopping the web test server...');
        server.proc.kill();
    };

    try {
        const browser = await puppeteer.launch();
        try {
            const page = await browser.newPage();
            const completedPromise = new Promise((resolve, reject) => {
                page.on("console", msg => {
                    const exitCode = /^~~EXIT\((\d+)\)~~$/.exec(msg.text());
                    if(exitCode) {
                        const code = parseInt(exitCode[1]);
                        if(code === 0) {
                            resolve();
                        } else {
                            reject(`Failed integration test suite with exit code ${code}`);
                        }
                    }
                    else {
                        console.log(`browser: ${msg.text()}`);
                    }
                });
            });

            await page.goto('http://localhost:7490/phantomtest.html');
            await completedPromise;
        }
        finally {
            await browser.close();
        }
    }
    finally {
        stopWebServer();
    }
});

gulp.task('run_tests',
    gulp.series(
        'run_node_tests',
        'bible_resources',
        'run_browser_tests'));

gulp.task('watch_tests', function watch_tests() {
    return gulp.watch("out/dbg/last-compiled.txt",
        gulp.series('run_tests'));
});

gulp.task('make_build_dir', mkdir_task(build_dir));

gulp.task('make_temp_votd_dir', mkdir_task(temp_votd_dir));

gulp.task('clean', function del_files() {
    return del([path.join(build_dir, '**'), '!' + build_dir], { cwd: root_dir });
});

gulp.task('copy_jasmine',
    gulp.parallel(
        copy_task(jasmine.cssFiles, jasmine_dir, jasmine.path),
        copy_task(jasmine.jsFiles, jasmine_dir, jasmine.path),
        copy_task(jasmine.bootFiles, jasmine_dir, jasmine.bootDir),
        copy_task('jasmine_favicon.png', jasmine_dir, jasmine.imagesDir)));

gulp.task('copy_votd_tests', copy_task('src/votd/test/**', test_dir));

gulp.task('verseoftheday',
    biblecli_task('verseoftheday',
        '--parser', config.bible_parser,
        '--input', config.bible_src,
        'src/votd/verse-list.md',
        temp_votd_dir));

gulp.task('build_votd_js',
    gulp.series(
        'make_temp_votd_dir',
        'verseoftheday',
        gulp.parallel(
            copy_task('client.html', votd_dir, temp_votd_dir),
            () => gulp.src(path.join(temp_votd_dir, 'votd.js'))
                .pipe(uglify())
                .pipe(gulp.dest(votd_dir)))));

gulp.task('build_votd',
    gulp.parallel(
        'copy_jasmine',
        'copy_votd_tests',
        'build_votd_js'));

gulp.task('copy_svg', copy_task('artwork/*.svg', build_dir));

gulp.task('copy_png', copy_task('artwork/*.png', build_dir));

gulp.task('copy_favicon', copy_task('artwork/favicon.ico', build_dir));

less.logger.addListener({
    info: msg => console.log("less: " + msg),
    warn: msg => console.warn("less: " + msg),
    error: msg => console.error("less: " + msg)
});

gulp.task('css',
    function compile_less() {
        function read() {
            return new Promise(
                (resolve, reject) =>
                    fs.readFile(path.join(root_dir, 'src/content/css/styles.less'), "utf8",
                        (err, data) => {
                            if(err) reject(err);
                            else resolve(data);
                        }));
        }

        function compile(data) {
            let opts = {
                compress: false,
                filename: "styles.less",
                sourceMap: {
                    sourceMapInputFilename: "styles.less",
                    sourceMapOutputFilename: "styles.css",
                    sourceMapFullFilename: "styles.css.map",
                    sourceMapFilename: "styles.css.map",
                    sourceMapBasepath: "",
                    sourceMapRootpath: ""
                }
            };
            return less.render(data, opts);
        }

        function write(filename, data) {
            return new Promise(
                (resolve, reject) =>
                    fs.writeFile(path.join(build_dir, filename), data, (err, data) => {
                        if(err) reject(err);
                        else resolve(data);
                    }));
        }

        return read()
            .then(input =>
                Promise.all([
                    compile(input)
                        .then(output => Promise.all([
                            write("styles.css", output.css),
                            write("styles.css.map", output.map)])),
                    write("styles.less", input)]));
    });

gulp.task('static_html',
    biblecli_task('static',
        '--parser', config.bible_parser,
        '--input', config.bible_src,
        '--canonical', config.canonical,
        '--baseurl', config.baseurl,
        (config.allowrobots ? "--allowrobots" : null),
        content_dir,
        build_dir));

gulp.task('markdown_html',
    biblecli_task('markdown', content_dir, build_dir));

gulp.task('static_javascript',
    biblecli_task('javascript', content_dir, build_dir));

gulp.task('sitemap',
    biblecli_task('sitemap',
        '--baseurl', config.baseurl,
        build_dir));

gulp.task('copy_kj_tests', copy_task('src/content/test/**', test_dir));

gulp.task('build',
    gulp.series(
        'clean',
        'make_build_dir',
        gulp.parallel(
            'css',
            'build_votd',
            'copy_svg',
            'copy_png',
            'copy_favicon',
            'static_javascript',
            'copy_jasmine',
            'copy_kj_tests'),
        gulp.parallel(
            'static_html',
            'markdown_html'),
        'sitemap'));

gulp.task('build_browser_test',
    gulp.series(
        'make_build_dir',
        gulp.parallel(
            'copy_kj_tests',
            'copy_jasmine',
            'static_javascript')));

gulp.task('bucketsync',
    biblecli_task('bucketsync',
        '--profile', config.profile,
        '--region', config.region,
        '--bucket', config.bucket,
        build_dir));

gulp.task('default',
   gulp.parallel(
    'compile_dbg',
    'watch_tests'));
