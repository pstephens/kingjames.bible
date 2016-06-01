function addDays(dt, days) {
    var dat = new Date(dt.valueOf());
    dat.setDate(dat.getDate() + days);
    return dat;
}

describe("getVerseFromDate", function() {
    var a = [],
        b, c, d, 
        cnt = 0;

    it("should return an integer between 0 and cnt exclusive given enough dates", function() {
        for(b = 0; b < 100; ++b) {
            d = addDays(new Date(2012, 1, 1), b);
            c = votd.getVerseFromDate(d, 5);
            expect(c).toBeGreaterThan(-1);
            expect(c).toBeLessThan(5);
            console.log("c = " + c);
            expect(c === Math.floor(c)).toBeTruthy();
            a[c] = true;
        }
    });

    it("should cover each index at least once given 100 consecutive dates", function() {
        for(b = 0; b < 5; ++b) {
            if(a[b]) {
                cnt++;
            }
        }
        expect(cnt).toEqual(5);
    });
});