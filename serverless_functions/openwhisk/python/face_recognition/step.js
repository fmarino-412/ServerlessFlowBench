const composer = require('openwhisk-composer')

module.exports = composer.try(
    composer.seq(   '__PLACEHOLDER__',
                    '__PLACEHOLDER__',
                    composer.if('__PLACEHOLDER__',
                            composer.if('__PLACEHOLDER__',
                                    '__PLACEHOLDER__',
                                    '__PLACEHOLDER__'),
                            '__PLACEHOLDER__')),
    '__PLACEHOLDER__')

// ORDER:
// input provider
// image recognition
// face checker
// anger detection
// anger
// no anger
// not face
// error