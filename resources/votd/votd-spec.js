function addDays(dt, days) {
    var dat = new Date(dt.valueOf());
    dat.setDate(dat.getDate() + days);
    return dat;
}

describe("getVerseFromDateAndHostname", function() {

    it("should return an integer between 0 and cnt exclusive given enough dates", function() {
        var a = [],
            b, c, d,
            cnt = 0;
        for(b = 0; b < 100; ++b) {
            d = addDays(new Date(2012, 1, 1), b);
            c = votd.getVerseFromDateAndHostname(d, "", 5);
            expect(c).toBeGreaterThan(-1);
            expect(c).toBeLessThan(5);
            expect(c === Math.floor(c)).toBeTruthy();
        }
    });

    it("should cover each index at least once given 100 consecutive dates", function() {
        var a = [],
            b, c, d,
            cnt = 0;
        for(b = 0; b < 100; ++b) {
            d = addDays(new Date(2012, 1, 1), b);
            c = votd.getVerseFromDateAndHostname(d, "", 5);
            a[c] = (a[c] || 0) + 1;
        }
        for(b = 0; b < 5; ++b) {
            if(a[b]) {
                cnt++;
            }
        }
        expect(cnt).toEqual(5);
    });

    it("should return the same index for different times of the day", function() {
        function addMs(dt, ms) { return new Date(dt.getTime() + ms); }
        var d = new Date(2005, 6, 2);
        var idx = votd.getVerseFromDateAndHostname(d, "", 100);
        expect(votd.getVerseFromDateAndHostname(addMs(d, 1), "", 100)).toEqual(idx);
        expect(votd.getVerseFromDateAndHostname(addMs(d, 1000*60*60), "", 100)).toEqual(idx);
        expect(votd.getVerseFromDateAndHostname(addMs(d, 1000*60*60*24 - 1), "", 100)).toEqual(idx);
        expect(votd.getVerseFromDateAndHostname(addMs(d, 1000*60*60*24), "", 100)).not.toEqual(idx);
        expect(votd.getVerseFromDateAndHostname(addMs(d, -1), "", 100)).not.toEqual(idx);
    });

    it("should return different indexes for different hostnames", function() {
        var d = new Date(2005, 6, 2);
        var idx1 = votd.getVerseFromDateAndHostname(d, "host1.com", 100);
        var idx2 = votd.getVerseFromDateAndHostname(d, "host2.com", 100);
        expect(idx1).not.toEqual(idx2);
    });

});

describe("renderVerses", function() {
    var url = "https://kingjames.bible/"

    it("should render a single verse", function() {
        var innerHtml = votd.renderVerses(url, ['II Chronicles 7','14 If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.']);
        expect(innerHtml).toEqual('<p><a href="https://kingjames.bible/II-Chronicles-7#14">II Chronicles 7:14</a> If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.</p>');
    });

    it("should render italics", function() {
        var innerHtml = votd.renderVerses(url, ['Psalms 119', '103 How sweet are thy words unto my taste! [yea, sweeter] than honey to my mouth!']);
        expect(innerHtml).toEqual('<p><a href="https://kingjames.bible/Psalms-119#103">Psalms 119:103</a> How sweet are thy words unto my taste! <i>yea, sweeter</i> than honey to my mouth!</p>');
    });

    it("should render multiple verses", function() {
        var innerHtml = votd.renderVerses(url, ['Lamentations 3', "22 [It is of] the LORD'S mercies that we are not consumed, because his compassions fail not.", "23 [They are] new every morning: great [is] thy faithfulness."]);
        expect(innerHtml).toEqual('<p><a href="https://kingjames.bible/Lamentations-3#22">Lamentations 3:22</a> <i>It is of</i> the LORD\'S mercies that we are not consumed, because his compassions fail not.</p><p><a href="https://kingjames.bible/Lamentations-3#23">23</a> <i>They are</i> new every morning: great <i>is</i> thy faithfulness.</p>');
    });

});

describe("renderVersesToElement", function() {
    var url = "https://kingjames.bible/"

    it("should set innerHTML of element", function() {
        var el = document.getElementById("votd");
        votd.renderVersesToElement("votd", [
            ['II Chronicles 7','14 If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.'],
            ['Psalms 119', '103 How sweet are thy words unto my taste! [yea, sweeter] than honey to my mouth!'],
            ['Lamentations 3', "22 [It is of] the LORD'S mercies that we are not consumed, because his compassions fail not.", "23 [They are] new every morning: great [is] thy faithfulness."]],
            new Date(2016, 5, 3),
            url,
            "");

        expect(el.innerHTML).toEqual('<p><a href="https://kingjames.bible/II-Chronicles-7#14">II Chronicles 7:14</a> If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.</p>');
    });
});