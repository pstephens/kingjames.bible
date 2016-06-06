(function(window, document) {
    var votdVar = window['VotdObject'] || 'votd';
    var votd = window[votdVar] = window[votdVar] || {};
    votd.verses = [
  ['Genesis 1','1 In the beginning God created the heaven and the earth.'],
  ['Genesis 8','22 While the earth remaineth, seedtime and harvest, and cold and heat, and summer and winter, and day and night shall not cease.'],
  ['II Chronicles 7','14 If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land.'],
  ['Psalms 1','1 Blessed [is] the man that walketh not in the counsel of the ungodly, nor standeth in the way of sinners, nor sitteth in the seat of the scornful.','2 But his delight [is] in the law of the LORD; and in his law doth he meditate day and night.'],
  ['Psalms 4','3 But know that the LORD hath set apart him that is godly for himself: the LORD will hear when I call unto him.'],
  ['Psalms 37','1 Fret not thyself because of evildoers, neither be thou envious against the workers of iniquity.','2 For they shall soon be cut down like the grass, and wither as the green herb.'],
  ['Psalms 37','4 Delight thyself also in the LORD; and he shall give thee the desires of thine heart.'],
  ['Psalms 37','7 Rest in the LORD, and wait patiently for him: fret not thyself because of him who prospereth in his way, because of the man who bringeth wicked devices to pass.'],
  ['Psalms 73','25 Whom have I in heaven [but thee]? and [there is] none upon earth [that] I desire beside thee.','26 My flesh and my heart faileth: [but] God [is] the strength of my heart, and my portion for ever.'],
  ['Psalms 73','28 But [it is] good for me to draw near to God: I have put my trust in the Lord GOD, that I may declare all thy works.'],
  ['Psalms 90','10 The days of our years [are] threescore years and ten; and if by reason of strength [they be] fourscore years, yet [is] their strength labour and sorrow; for it is soon cut off, and we fly away.'],
  ['Psalms 90','12 So teach [us] to number our days, that we may apply [our] hearts unto wisdom.'],
  ['Psalms 91','1 He that dwelleth in the secret place of the most High shall abide under the shadow of the Almighty.','2 I will say of the LORD, [He is] my refuge and my fortress: my God; in him will I trust.'],
  ['Psalms 92','1 [It is a] good [thing] to give thanks unto the LORD, and to sing praises unto thy name, O most High:','2 To shew forth thy lovingkindness in the morning, and thy faithfulness every night,'],
  ['Psalms 92','4 For thou, LORD, hast made me glad through thy work: I will triumph in the works of thy hands.','5 O LORD, how great are thy works! [and] thy thoughts are very deep.'],
  ['Psalms 92','13 Those that be planted in the house of the LORD shall flourish in the courts of our God.','14 They shall still bring forth fruit in old age; they shall be fat and flourishing;'],
  ['Psalms 94','12 Blessed [is] the man whom thou chastenest, O LORD, and teachest him out of thy law;','13 That thou mayest give him rest from the days of adversity, until the pit be digged for the wicked.'],
  ['Psalms 94','19 In the multitude of my thoughts within me thy comforts delight my soul.'],
  ['Psalms 95','1 O come, let us sing unto the LORD: let us make a joyful noise to the rock of our salvation.','2 Let us come before his presence with thanksgiving, and make a joyful noise unto him with psalms.'],
  ['Psalms 95','3 For the LORD [is] a great God, and a great King above all gods.','4 In his hand [are] the deep places of the earth: the strength of the hills [is] his also.'],
  ['Psalms 119','103 How sweet are thy words unto my taste! [yea, sweeter] than honey to my mouth!'],
  ['Proverbs 10','23 [It is] as sport to a fool to do mischief: but a man of understanding hath wisdom.'],
  ['Proverbs 11','30 The fruit of the righteous [is] a tree of life; and he that winneth souls [is] wise.'],
  ['Proverbs 11','4 Riches profit not in the day of wrath: but righteousness delivereth from death.'],
  ['Proverbs 12','25 Heaviness in the heart of man maketh it stoop: but a good word maketh it glad.'],
  ['Proverbs 14','7 Go from the presence of a foolish man, when thou perceivest not [in him] the lips of knowledge.'],
  ['Proverbs 14','12 There is a way which seemeth right unto a man, but the end thereof [are] the ways of death.'],
  ['Proverbs 15','13 A merry heart maketh a cheerful countenance: but by sorrow of the heart the spirit is broken.'],
  ['Proverbs 17','22 A merry heart doeth good [like] a medicine: but a broken spirit drieth the bones.'],
  ['Ecclesiastes 9','17 The words of wise [men are] heard in quiet more than the cry of him that ruleth among fools.'],
  ['Ecclesiastes 10','20 Curse not the king, no not in thy thought; and curse not the rich in thy bedchamber: for a bird of the air shall carry the voice, and that which hath wings shall tell the matter.'],
  ['Lamentations 3','22 [It is of] the LORD\'S mercies that we are not consumed, because his compassions fail not.','23 [They are] new every morning: great [is] thy faithfulness.'],
  ['Matthew 1','21 And she shall bring forth a son, and thou shalt call his name JESUS: for he shall save his people from their sins.'],
  ['Mark 14','38 Watch ye and pray, lest ye enter into temptation. The spirit truly [is] ready, but the flesh [is] weak.'],
  ['Acts 3','19 Repent ye therefore, and be converted, that your sins may be blotted out, when the times of refreshing shall come from the presence of the Lord;'],
  ['Acts 4','13 Now when they saw the boldness of Peter and John, and perceived that they were unlearned and ignorant men, they marvelled; and they took knowledge of them, that they had been with Jesus.'],
  ['Romans 5','8 But God commendeth his love toward us, in that, while we were yet sinners, Christ died for us.','9 Much more then, being now justified by his blood, we shall be saved from wrath through him.'],
  ['Romans 8','1 [There is] therefore now no condemnation to them which are in Christ Jesus, who walk not after the flesh, but after the Spirit.'],
  ['Romans 8','28 And we know that all things work together for good to them that love God, to them who are the called according to [his] purpose.'],
  ['Romans 12','1 I beseech you therefore, brethren, by the mercies of God, that ye present your bodies a living sacrifice, holy, acceptable unto God, [which is] your reasonable service.']
];

    // A Javascript implementaion of Richard Brent's Xorgens xor4096 algorithm.
    // http://arxiv.org/pdf/1004.3115v1.pdf
    // https://github.com/davidbau/seedrandom/blob/released/lib/xor4096.js
    !function(a,b,c){function d(a){function b(a,b){var c,d,e,f,g,h=[],i=128;for(b===(0|b)?(d=b,b=null):(b+="\x00",d=0,i=Math.max(i,b.length)),e=0,f=-32;i>f;++f)b&&(d^=b.charCodeAt((f+32)%b.length)),0===f&&(g=d),d^=d<<10,d^=d>>>15,d^=d<<4,d^=d>>>13,f>=0&&(g=g+1640531527|0,c=h[127&f]^=d+g,e=0==c?e+1:0);for(e>=128&&(h[127&(b&&b.length||0)]=-1),e=127,f=512;f>0;--f)d=h[e+34&127],c=h[e=e+1&127],d^=d<<13,c^=c<<17,d^=d>>>15,c^=c>>>12,h[e]=d^c;a.w=g,a.X=h,a.i=e}var c=this;c.next=function(){var a,b,d=c.w,e=c.X,f=c.i;return c.w=d=d+1640531527|0,b=e[f+34&127],a=e[f=f+1&127],b^=b<<13,a^=a<<17,b^=b>>>15,a^=a>>>12,b=e[f]=b^a,c.i=f,b+(d^d>>>16)|0},b(c,a)}function e(a,b){return b.i=a.i,b.w=a.w,b.X=a.X.slice(),b}function f(a,b){null==a&&(a=+new Date);var c=new d(a),f=b&&b.state,g=function(){return(c.next()>>>0)/4294967296};return g["double"]=function(){do var a=c.next()>>>11,b=(c.next()>>>0)/4294967296,d=(a+b)/(1<<21);while(0===d);return d},g.int32=c.next,g.quick=g,f&&(f.X&&e(f,c),g.state=function(){return e(c,{})}),g}b&&b.exports?b.exports=f:c&&c.amd?c(function(){return f}):a.xor4096=f}(votd,"object"==typeof module&&module,"function"==typeof define&&define);

    function hashedIndex(s, maxEx) {
        var prng = votd.xor4096(s);
        return Math.floor(prng.double() * maxEx);
    }

    function calcUrl(baseUrl, chap) {
        return baseUrl + chap.replace(/\s/g, "-");
    }

    votd.getVerseFromDate = function getVerseFromDate(dt, cnt) {
        var dayNum = dt.getFullYear() * 10000 + dt.getMonth() * 100 + dt.getDate()
        console.log("dayNum: " + dayNum);
        return hashedIndex(dayNum, cnt);
    };

    votd.renderVerses = function renderVerses(baseUrl, verses) {
        var b = "",
            url = calcUrl(baseUrl, verses[0]),
            i, num, matches, txt;

        for(i = 1; i < verses.length; ++i) {
            matches = verses[i].match(/^(\d+) (.*)$/);
            num = parseInt(matches[1]);
            txt = matches[2];
            b += '<p><a href="' + url + '#' + num + '">';
            if(i == 1) {
                b += verses[0] + ':';
            }
            b += num + '</a> ';
            b += txt.replace(/\[/g, "<i>").replace(/\]/g, "</i>");
            b += '</p>';
        }

        return b;
    };

    votd.renderVersesToElement = function renderVersesToElement(id, allVerses, dt, baseUrl) {
        var el, i, v, html;

        if(!id) {
            return;
        }

        el = document.getElementById(id);
        if(!el) {
            return;
        }

        i = votd.getVerseFromDate(dt, allVerses.length);
        v = allVerses[i];
        html = votd.renderVerses(baseUrl, v);

        el.innerHTML = html;
    }

    votd.renderVerseOfTheDayToElement = function renderVersesToElement() {
        votd.renderVersesToElement(
            votd.i,
            votd.verses,
            new Date(),
            "https://kingjames.bible/");
    }

    votd.renderVerseOfTheDayToElement();

})(window, document);