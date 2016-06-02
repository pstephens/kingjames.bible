function addDays(dt, days) {
    var dat = new Date(dt.valueOf());
    dat.setDate(dat.getDate() + days);
    return dat;
}

describe("getVerseFromDate", function() {

    it("should return an integer between 0 and cnt exclusive given enough dates", function() {
        var a = [],
            b, c, d,
            cnt = 0;
        for(b = 0; b < 100; ++b) {
            d = addDays(new Date(2012, 1, 1), b);
            c = votd.getVerseFromDate(d, 5);
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
            c = votd.getVerseFromDate(d, 5);
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
        var idx = votd.getVerseFromDate(d, 100);
        expect(votd.getVerseFromDate(addMs(d, 1), 100)).toEqual(idx);
        expect(votd.getVerseFromDate(addMs(d, 1000*60*60), 100)).toEqual(idx);
        expect(votd.getVerseFromDate(addMs(d, 1000*60*60*24 - 1), 100)).toEqual(idx);
        expect(votd.getVerseFromDate(addMs(d, 1000*60*60*24), 100)).not.toEqual(idx);
        expect(votd.getVerseFromDate(addMs(d, -1), 100)).not.toEqual(idx);
    });

});

describe("renderVerses", function() {
    var url = "https://kingjames.bible/"

    it("should render single verse.", function() {
        var innerHtml = votd.renderVerses(url, ['II Chronicles 7','14 If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.']);
        expect(innerHtml).toEqual('<p><a href="https://kingjames.bible/II-Chronicles-7#14">II Chronicles 7:14</a> If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.</p>');
    });

});