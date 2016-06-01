(function(window) {
    var votdVar = window['VotdObject'] || 'votd';
    var votd = window[votdVar] = window[votdVar] || {};
    votd.verses = [
        ['II Chronicles 7','14 If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.'],
        ['Psalms 119', '103 How sweet are thy words unto my taste! [yea, sweeter] than honey to my mouth!'],
        ['Lamentations 3', "22 [It is of] the LORD'S mercies that we are not consumed, because his compassions fail not.", "23 [They are] new every morning: great [is] thy faithfulness."],
        ['Matthew 1', "21 And she shall bring forth a son, and thou shalt call his name JESUS: for he shall save his people from their sins."]];

    // A Javascript implementaion of Richard Brent's Xorgens xor4096 algorithm.
    // http://arxiv.org/pdf/1004.3115v1.pdf
    // https://github.com/davidbau/seedrandom/blob/released/lib/xor4096.js
    !function(a,b,c){function d(a){function b(a,b){var c,d,e,f,g,h=[],i=128;for(b===(0|b)?(d=b,b=null):(b+="\x00",d=0,i=Math.max(i,b.length)),e=0,f=-32;i>f;++f)b&&(d^=b.charCodeAt((f+32)%b.length)),0===f&&(g=d),d^=d<<10,d^=d>>>15,d^=d<<4,d^=d>>>13,f>=0&&(g=g+1640531527|0,c=h[127&f]^=d+g,e=0==c?e+1:0);for(e>=128&&(h[127&(b&&b.length||0)]=-1),e=127,f=512;f>0;--f)d=h[e+34&127],c=h[e=e+1&127],d^=d<<13,c^=c<<17,d^=d>>>15,c^=c>>>12,h[e]=d^c;a.w=g,a.X=h,a.i=e}var c=this;c.next=function(){var a,b,d=c.w,e=c.X,f=c.i;return c.w=d=d+1640531527|0,b=e[f+34&127],a=e[f=f+1&127],b^=b<<13,a^=a<<17,b^=b>>>15,a^=a>>>12,b=e[f]=b^a,c.i=f,b+(d^d>>>16)|0},b(c,a)}function e(a,b){return b.i=a.i,b.w=a.w,b.X=a.X.slice(),b}function f(a,b){null==a&&(a=+new Date);var c=new d(a),f=b&&b.state,g=function(){return(c.next()>>>0)/4294967296};return g["double"]=function(){do var a=c.next()>>>11,b=(c.next()>>>0)/4294967296,d=(a+b)/(1<<21);while(0===d);return d},g.int32=c.next,g.quick=g,f&&(f.X&&e(f,c),g.state=function(){return e(c,{})}),g}b&&b.exports?b.exports=f:c&&c.amd?c(function(){return f}):a.xor4096=f}(votd,"object"==typeof module&&module,"function"==typeof define&&define);

    function hashedIndex(s, maxEx) {
        var prng = votd.xor4096(s);
        return Math.floor(prng.double() * maxEx);
    }

    votd.getVerseFromDate = function getVerseFromDate(dt, cnt) {
        var dayNum = dt.getFullYear() * 10000 + dt.getMonth() * 100 + dt.getDate()
        return hashedIndex(dayNum, cnt);
    };
    votd.renderVerses = function renderVerses(baseUrl, verses) {

    };

})(window);