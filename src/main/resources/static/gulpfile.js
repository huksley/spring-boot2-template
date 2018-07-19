var gulp = require('gulp');
var $ = require('gulp-load-plugins')();
var fs = require('fs');

// Global resources, directly accessed, not compiled
var resources = [
    "node_modules/font-awesome/fonts",
    "node_modules/font-awesome/css",
    "node_modules/foundation-sites/dist -> foundation/dist"

];

var sassPaths = [
    'node_modules/normalize.scss/sass',
    'node_modules/foundation-sites/scss',
    'node_modules/motion-ui/src',
    'node_modules/font-awesome/scss'
];

var dist = "vendor";

var console = require("console");
var browserify = require('browserify');
var babelify = require("babelify");
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var log = require('gulplog');
var uglify = require('gulp-uglify');
var aliasify = require('aliasify');
var vueify = require('vueify');
var sourcemaps = require('gulp-sourcemaps');
var transform = require('vinyl-transform');

gulp.task('css', function () {
    console.log("Running css task...");
    try {
        return gulp.src([ 'scss/app.scss', 'scss/login.scss', 'scss/user.scss' ])
            .pipe($.sass({
                includePaths: sassPaths,
                // outputStyle: 'compressed' // if css compressed **file size**
                outputStyle: 'expanded'
            })
                .on('error', $.sass.logError))
            .pipe($.autoprefixer({
                browsers: ['last 2 versions', 'ie >= 11']
            }))
            .pipe(sourcemaps.init({loadMaps: true}))
            .pipe(sourcemaps.write('./'))
            .pipe(gulp.dest('generated/css'));
    } catch (e) {
        console.log("ERROR running sass task: " + e);
    }
});

function jsOne(fname) {
    // set up the browserify instance on a task basis
    var b = browserify({
        entries: './js/' + fname,
        debug: true,
        // defining transforms here will avoid crashing your stream
        transform: [aliasify]
    });

    return b.bundle()
    // WHY TWO TIMES!?
        .pipe(source('./js/' + fname)) // destination file for browserify, relative to gulp.dest
        .pipe(buffer())
        .pipe(vueify())
        //.pipe(uglify())
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(sourcemaps.write('./'))
        .on('error', log.error)
        .pipe(gulp.dest('./generated'));
}

gulp.task('js', function () {
    console.log("Running js task...");
    try {
        jsOne('app.js');
        jsOne('login.js');
        jsOne('user.js');
    } catch (e) {
        console.log("ERROR running js task: " + e);
    }
});

gulp.task('resources', function () {
    console.log("Running resources task...");
    try {
        for (var i = 0; i < resources.length; i++) {
            var path = resources[i];
            var dst = path.replace(/^.*node_modules\//g, "");
            dst = dist + "/" + dst;
            if (path.indexOf("->") > 0) {
                dst = dist + "/" + path.substring(path.indexOf("->") + 2).trim();
                path = path.substring(0, path.indexOf("->"));
            }
            path = path.trim();
            dst = dst.trim();
            if (isfile(path)) {
                console.log("Copying file from " + path + " to " + dst);
                dst = dst.substring(0, dst.lastIndexOf("/"));
                gulp.src([path]).pipe(gulp.dest(dst));
            } else {
                if (isdir(path)) {
                    console.log("Copying resources from " + path + " to " + dst);
                    gulp.src([path + "/**"]).pipe(gulp.dest(dst));
                } else {
                    throw "Not a file or directory: " + path;
                }
            }
        }
    } catch (e) {
        console.log("ERROR running resources task: " + e);
    }
});

function isdir(dir) {
    var stats = fs.statSync(dir);
    return stats && stats.isDirectory();
}

function isfile(f) {
    var stats = fs.statSync(f);
    return stats && stats.isFile();
}

function havefile(f) {
    try {
        var stats = fs.statSync(f);
        return stats && stats.isFile();
    } catch (e) {
        return false;
    }
}

gulp.task('watch', [], function () {
    gulp.watch(['./js/*.js'], ['js']);
    gulp.watch(['./scss/*.scss'], ['css']);
});

console.log("Prepared tasks");
