(function(window) {
    var votdVar = window['VotdObject'] || 'votd';
    var votd = window[votdVar] = window[votdVar] || {};
    votd.verses = [
        ['II Chronicles 7','14 If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.'],
        ['Psalms 119', '103 How sweet are thy words unto my taste! [yea, sweeter] than honey to my mouth!'],
        ['Lamentations 3', "22 [It is of] the LORD'S mercies that we are not consumed, because his compassions fail not.", "23 [They are] new every morning: great [is] thy faithfulness."],
        ['Matthew 1', "21 And she shall bring forth a son, and thou shalt call his name JESUS: for he shall save his people from their sins."]];

    function hashedIndex(s, maxEx) {
        // based on http://indiegamr.com/generate-repeatable-random-numbers-in-js/
        // MAYBE: use the sin based alg here for more even distro: http://stackoverflow.com/questions/521295/javascript-random-seeds
        return ((s * 9301 + 49297) % 233280) % maxEx;
    }

    votd.getVerseFromDate = function getVerseFromDate(dt, cnt) {
        var dayNum = (dt.getTime() - (dt.getTimezoneOffset() * 60 * 1000)) / (24*60*60*1000);
        console.log("dayNum: " + dayNum);
        return hashedIndex(dayNum, cnt);
    };
    votd.renderVerses = function renderVerses(baseUrl, verses) {

    };

})(window);