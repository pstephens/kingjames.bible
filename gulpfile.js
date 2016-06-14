var _ =       require('lodash');
var cp =      require('child_process');
var del =     require('del');
var fs =      require('fs');
var gulp =    require('gulp');
var jasmine = require('jasmine-core').files;
var mkdirp =  require('mkdirp');
var os =      require('os');
var path =    require('path');
var phantom = require('phantomjs2');
var q =       require('q');

var root_dir = __dirname
var node_exec = process.execPath;
var out_bible_dir = path.join(root_dir, 'out/bible');
var out_prod = path.join(root_dir, 'out/prod');
var out_temp = path.join(root_dir, 'out/temp');
var out_votd_temp = path.join(out_temp, 'votd');
console.log(out_votd_temp);
var default_parser = 'staggs';
var default_bible_src = 'kjv-src/www.staggs.pair.com-kjbp/kjv.txt';

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
    var deferred = q.defer();
    args = args || [];
    options = Object.assign({ cwd: root_dir }, options || {});

    var proc = cp.spawn(command, args, options);
    proc.stdout.on('data', output_to(name, process.stdout));
    proc.stderr.on('data', output_to(name, process.stderr));
    proc.on('close', function(code) {
        if(code) {
            deferred.reject(new Error(name + ': failed with exit code ' + code));
        }
        else {
            deferred.resolve();
        }
    });
    proc.on('error', function(err) {
        deferred.reject(err);
    });

    return {
        proc: proc,
        promise: deferred.promise
    };
}

function exec(name, command, args) {
    return spawn(name, command, args, { shell: true });
}

function biblecli(cmd, args) {
    return spawn('biblecli-' + cmd, node_exec,
        _.concat(['biblecli.js', cmd], _.slice(arguments, 1))).promise
}

function biblecli_task(cmd, args) {
    args = _.slice(arguments);
    return function biblecli_task() {
        return biblecli.apply(null, args);
    }
}

function mkdir_task(dir) {
    return function mkdir_task(cb) {
        console.log('Creating dir ' + dir);
        mkdirp(dir, cb);
    }
}

gulp.task('compile_dbg', function() {
    return exec('lein', 'lein', ['cljsbuild', 'auto', 'dbg']).promise;
});

gulp.task('bible_resources_dir', mkdir_task(out_bible_dir));

gulp.task('bible_resources', gulp.series(
    'bible_resources_dir',
    biblecli_task('prepare', default_parser, default_bible_src, 'out/bible')
    ));

gulp.task('run_node_tests', function() {
    return spawn('node', node_exec, ['nodetest.js']).promise;
});

gulp.task('run_phantom_tests', function() {
    // launch the web server
    var server = biblecli('serve');

    // launch phantom.js
    return spawn('phantom', phantom.path, ['phantomtest.js', 'http://localhost:7490/phantomtest.html']).promise
        .finally(function() {
            console.log('Stopping the web test server...');
            server.proc.kill();
        });
});

gulp.task('run_tests',
    gulp.series(
        'run_node_tests',
        'bible_resources',
        'run_phantom_tests'));

gulp.task('watch_tests', function() {
    return gulp.watch("out/dbg/last-compiled.txt",
        gulp.series('run_tests'));
});

gulp.task('make_prod_dir', mkdir_task(out_prod));

gulp.task('make_votd_temp_dir', mkdir_task(out_votd_temp));

gulp.task('clean_prod', function() {
    return del([path.join(out_prod, '**'), '!' + out_prod], { cwd: root_dir });
});

gulp.task('copy_votd_jasmine',
    gulp.parallel(
        function copy_jasmine_files() {
            var files = _.concat(jasmine.cssFiles, jasmine.jsFiles);
            return gulp.src(files, { cwd: jasmine.path })
                    .pipe(gulp.dest(path.join(out_prod, "votd/jasmine")));
        },
        function copy_jasmine_boot() {
            return gulp.src(jasmine.bootFiles, { cwd: jasmine.bootDir })
                    .pipe(gulp.dest(path.join(out_prod, "votd/jasmine")));
        },
        function copy_jasmine_images() {
            return gulp.src('jasmine_favicon.png', { cwd: jasmine.imagesDir })
                    .pipe(gulp.dest(path.join(out_prod, "votd/jasmine")));
        }));

gulp.task('copy_votd_tests', function copy_votd_tests() {
    return gulp.src(path.join(root_dir, 'src/votd/test/**'))
            .pipe(gulp.dest(path.join(out_prod, "votd")));
});

gulp.task('build_votd_js',
    gulp.series(
        'make_votd_temp_dir',
        biblecli_task('verseoftheday', default_parser, default_bible_src,
            'src/votd/verse-list.md', out_votd_temp),
        function copy_votd_js() {
            return gulp.src(path.join(out_votd_temp, '**'))
                    .pipe(gulp.dest(path.join(out_prod, "votd")));
        }));

gulp.task('build_prod',
    gulp.series(
        'clean_prod',
        'make_prod_dir',
        gulp.parallel(
            biblecli_task('static', default_parser, default_bible_src, out_prod),
            'copy_votd_jasmine',
            'copy_votd_tests',
            'build_votd_js'
            )));

gulp.task('default',
   gulp.parallel(
    'compile_dbg',
    'watch_tests'));