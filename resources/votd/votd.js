(function(window, document) {
    var votdVar = window['VotdObject'] || 'votd';
    var votd = window[votdVar] = window[votdVar] || {};
    votd.verses = [
  ["Genesis 1","1 In the beginning God created the heaven and the earth."],
  ["Genesis 8","22 While the earth remaineth, seedtime and harvest, and cold and heat, and summer and winter, and day and night shall not cease."],
  ["Genesis 14","18 And Melchizedek king of Salem brought forth bread and wine: and he [was] the priest of the most high God.","19 And he blessed him, and said, Blessed [be] Abram of the most high God, possessor of heaven and earth:","20 And blessed be the most high God, which hath delivered thine enemies into thy hand. And he gave him tithes of all."],
  ["Genesis 15","1 After these things the word of the LORD came unto Abram in a vision, saying, Fear not, Abram: I [am] thy shield, [and] thy exceeding great reward."],
  ["Genesis 15","18 In the same day the LORD made a covenant with Abram, saying, Unto thy seed have I given this land, from the river of Egypt unto the great river, the river Euphrates:"],
  ["Genesis 47","9 And Jacob said unto Pharaoh, The days of the years of my pilgrimage [are] an hundred and thirty years: few and evil have the days of the years of my life been, and have not attained unto the days of the years of the life of my fathers in the days of their pilgrimage.","10 And Jacob blessed Pharaoh, and went out from before Pharaoh."],
  ["Exodus 3","14 And God said unto Moses, I AM THAT I AM: and he said, Thus shalt thou say unto the children of Israel, I AM hath sent me unto you.","15 And God said moreover unto Moses, Thus shalt thou say unto the children of Israel, The LORD God of your fathers, the God of Abraham, the God of Isaac, and the God of Jacob, hath sent me unto you: this [is] my name for ever, and this [is] my memorial unto all generations."],
  ["Exodus 4","10 And Moses said unto the LORD, O my Lord, I [am] not eloquent, neither heretofore, nor since thou hast spoken unto thy servant: but I [am] slow of speech, and of a slow tongue.","11 And the LORD said unto him, Who hath made man's mouth? or who maketh the dumb, or deaf, or the seeing, or the blind? have not I the LORD?"],
  ["Exodus 5","1 And afterward Moses and Aaron went in, and told Pharaoh, Thus saith the LORD God of Israel, Let my people go, that they may hold a feast unto me in the wilderness.","2 And Pharaoh said, Who [is] the LORD, that I should obey his voice to let Israel go? I know not the LORD, neither will I let Israel go."],
  ["Exodus 15","1 Then sang Moses and the children of Israel this song unto the LORD, and spake, saying, I will sing unto the LORD, for he hath triumphed gloriously: the horse and his rider hath he thrown into the sea.","2 The LORD [is] my strength and song, and he is become my salvation: he [is] my God, and I will prepare him an habitation; my father's God, and I will exalt him."],
  ["Exodus 20","12 Honour thy father and thy mother: that thy days may be long upon the land which the LORD thy God giveth thee."],
  ["Numbers 6","24 The LORD bless thee, and keep thee:","25 The LORD make his face shine upon thee, and be gracious unto thee:","26 The LORD lift up his countenance upon thee, and give thee peace."],
  ["II Chronicles 7","14 If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven, and will forgive their sin, and will heal their land."],
  ["Psalms 1","1 Blessed [is] the man that walketh not in the counsel of the ungodly, nor standeth in the way of sinners, nor sitteth in the seat of the scornful.","2 But his delight [is] in the law of the LORD; and in his law doth he meditate day and night."],
  ["Psalms 4","3 But know that the LORD hath set apart him that is godly for himself: the LORD will hear when I call unto him."],
  ["Psalms 37","1 Fret not thyself because of evildoers, neither be thou envious against the workers of iniquity.","2 For they shall soon be cut down like the grass, and wither as the green herb."],
  ["Psalms 37","4 Delight thyself also in the LORD; and he shall give thee the desires of thine heart."],
  ["Psalms 37","7 Rest in the LORD, and wait patiently for him: fret not thyself because of him who prospereth in his way, because of the man who bringeth wicked devices to pass."],
  ["Psalms 73","25 Whom have I in heaven [but thee]? and [there is] none upon earth [that] I desire beside thee.","26 My flesh and my heart faileth: [but] God [is] the strength of my heart, and my portion for ever."],
  ["Psalms 73","28 But [it is] good for me to draw near to God: I have put my trust in the Lord GOD, that I may declare all thy works."],
  ["Psalms 90","10 The days of our years [are] threescore years and ten; and if by reason of strength [they be] fourscore years, yet [is] their strength labour and sorrow; for it is soon cut off, and we fly away."],
  ["Psalms 90","12 So teach [us] to number our days, that we may apply [our] hearts unto wisdom."],
  ["Psalms 91","1 He that dwelleth in the secret place of the most High shall abide under the shadow of the Almighty.","2 I will say of the LORD, [He is] my refuge and my fortress: my God; in him will I trust."],
  ["Psalms 92","1 [It is a] good [thing] to give thanks unto the LORD, and to sing praises unto thy name, O most High:","2 To shew forth thy lovingkindness in the morning, and thy faithfulness every night,"],
  ["Psalms 92","4 For thou, LORD, hast made me glad through thy work: I will triumph in the works of thy hands.","5 O LORD, how great are thy works! [and] thy thoughts are very deep."],
  ["Psalms 92","13 Those that be planted in the house of the LORD shall flourish in the courts of our God.","14 They shall still bring forth fruit in old age; they shall be fat and flourishing;"],
  ["Psalms 94","12 Blessed [is] the man whom thou chastenest, O LORD, and teachest him out of thy law;","13 That thou mayest give him rest from the days of adversity, until the pit be digged for the wicked."],
  ["Psalms 94","19 In the multitude of my thoughts within me thy comforts delight my soul."],
  ["Psalms 95","1 O come, let us sing unto the LORD: let us make a joyful noise to the rock of our salvation.","2 Let us come before his presence with thanksgiving, and make a joyful noise unto him with psalms."],
  ["Psalms 95","3 For the LORD [is] a great God, and a great King above all gods.","4 In his hand [are] the deep places of the earth: the strength of the hills [is] his also."],
  ["Psalms 96","1 O sing unto the LORD a new song: sing unto the LORD, all the earth.","2 Sing unto the LORD, bless his name; shew forth his salvation from day to day."],
  ["Psalms 96","3 Declare his glory among the heathen, his wonders among all people.","4 For the LORD [is] great, and greatly to be praised: he [is] to be feared above all gods."],
  ["Psalms 96","9 O worship the LORD in the beauty of holiness: fear before him, all the earth."],
  ["Psalms 97","10 Ye that love the LORD, hate evil: he preserveth the souls of his saints; he delivereth them out of the hand of the wicked."],
  ["Psalms 97","11 Light is sown for the righteous, and gladness for the upright in heart.","12 Rejoice in the LORD, ye righteous; and give thanks at the remembrance of his holiness."],
  ["Psalms 98","1 O sing unto the LORD a new song; for he hath done marvellous things: his right hand, and his holy arm, hath gotten him the victory."],
  ["Psalms 98","2 The LORD hath made known his salvation: his righteousness hath he openly shewed in the sight of the heathen."],
  ["Psalms 98","4 Make a joyful noise unto the LORD, all the earth: make a loud noise, and rejoice, and sing praise."],
  ["Psalms 98","5 Sing unto the LORD with the harp; with the harp, and the voice of a psalm.","6 With trumpets and sound of cornet make a joyful noise before the LORD, the King."],
  ["Psalms 100","1 Make a joyful noise unto the LORD, all ye lands.","2 Serve the LORD with gladness: come before his presence with singing."],
  ["Psalms 100","3 Know ye that the LORD he [is] God: [it is] he [that] hath made us, and not we ourselves; [we are] his people, and the sheep of his pasture."],
  ["Psalms 101","1 I will sing of mercy and judgment: unto thee, O LORD, will I sing."],
  ["Psalms 101","3 I will set no wicked thing before mine eyes: I hate the work of them that turn aside; [it] shall not cleave to me."],
  ["Psalms 101","7 He that worketh deceit shall not dwell within my house: he that telleth lies shall not tarry in my sight."],
  ["Psalms 102","1 Hear my prayer, O LORD, and let my cry come unto thee.","2 Hide not thy face from me in the day [when] I am in trouble; incline thine ear unto me: in the day [when] I call answer me speedily."],
  ["Psalms 102","11 My days [are] like a shadow that declineth; and I am withered like grass.","12 But thou, O LORD, shalt endure for ever; and thy remembrance unto all generations."],
  ["Psalms 102","13 Thou shalt arise, [and] have mercy upon Zion: for the time to favour her, yea, the set time, is come.","14 For thy servants take pleasure in her stones, and favour the dust thereof."],
  ["Psalms 102","19 For he hath looked down from the height of his sanctuary; from heaven did the LORD behold the earth;","20 To hear the groaning of the prisoner; to loose those that are appointed to death;","21 To declare the name of the LORD in Zion, and his praise in Jerusalem;"],
  ["Psalms 103","1 Bless the LORD, O my soul: and all that is within me, [bless] his holy name."],
  ["Psalms 103","2 Bless the LORD, O my soul, and forget not all his benefits:","3 Who forgiveth all thine iniquities; who healeth all thy diseases;"],
  ["Psalms 103","6 The LORD executeth righteousness and judgment for all that are oppressed."],
  ["Psalms 103","8 The LORD [is] merciful and gracious, slow to anger, and plenteous in mercy.","9 He will not always chide: neither will he keep [his anger] for ever."],
  ["Psalms 103","10 He hath not dealt with us after our sins; nor rewarded us according to our iniquities.","11 For as the heaven is high above the earth, [so] great is his mercy toward them that fear him."],
  ["Psalms 103","12 As far as the east is from the west, [so] far hath he removed our transgressions from us."],
  ["Psalms 103","13 Like as a father pitieth [his] children, [so] the LORD pitieth them that fear him.","14 For he knoweth our frame; he remembereth that we [are] dust."],
  ["Psalms 103","15 [As for] man, his days [are] as grass: as a flower of the field, so he flourisheth.","16 For the wind passeth over it, and it is gone; and the place thereof shall know it no more.","17 But the mercy of the LORD [is] from everlasting to everlasting upon them that fear him, and his righteousness unto children's children;"],
  ["Psalms 103","19 The LORD hath prepared his throne in the heavens; and his kingdom ruleth over all."],
  ["Psalms 103","20 Bless the LORD, ye his angels, that excel in strength, that do his commandments, hearkening unto the voice of his word."],
  ["Psalms 103","21 Bless ye the LORD, all [ye] his hosts; [ye] ministers of his, that do his pleasure.","22 Bless the LORD, all his works in all places of his dominion: bless the LORD, O my soul."],
  ["Psalms 104","1 Bless the LORD, O my soul. O LORD my God, thou art very great; thou art clothed with honour and majesty."],
  ["Psalms 104","10 He sendeth the springs into the valleys, [which] run among the hills.","11 They give drink to every beast of the field: the wild asses quench their thirst.","12 By them shall the fowls of the heaven have their habitation, [which] sing among the branches."],
  ["Psalms 104","14 He causeth the grass to grow for the cattle, and herb for the service of man: that he may bring forth food out of the earth;"],
  ["Psalms 104","16 The trees of the LORD are full [of sap]; the cedars of Lebanon, which he hath planted;","17 Where the birds make their nests: [as for] the stork, the fir trees [are] her house."],
  ["Psalms 104","19 He appointed the moon for seasons: the sun knoweth his going down.","20 Thou makest darkness, and it is night: wherein all the beasts of the forest do creep [forth]."],
  ["Psalms 104","24 O LORD, how manifold are thy works! in wisdom hast thou made them all: the earth is full of thy riches."],
  ["Psalms 104","31 The glory of the LORD shall endure for ever: the LORD shall rejoice in his works."],
  ["Psalms 104","33 I will sing unto the LORD as long as I live: I will sing praise to my God while I have my being.","34 My meditation of him shall be sweet: I will be glad in the LORD."],
  ["Psalms 104","35 Let the sinners be consumed out of the earth, and let the wicked be no more. Bless thou the LORD, O my soul. Praise ye the LORD."],
  ["Psalms 105","1 O give thanks unto the LORD; call upon his name: make known his deeds among the people.","2 Sing unto him, sing psalms unto him: talk ye of all his wondrous works."],
  ["Psalms 105","3 Glory ye in his holy name: let the heart of them rejoice that seek the LORD.","5 Remember his marvellous works that he hath done; his wonders, and the judgments of his mouth;"],
  ["Psalms 106","1 Praise ye the LORD. O give thanks unto the LORD; for [he is] good: for his mercy [endureth] for ever.","2 Who can utter the mighty acts of the LORD? [who] can shew forth all his praise?"],
  ["Psalms 106","47 Save us, O LORD our God, and gather us from among the heathen, to give thanks unto thy holy name, [and] to triumph in thy praise."],
  ["Psalms 106","48 Blessed [be] the LORD God of Israel from everlasting to everlasting: and let all the people say, Amen. Praise ye the LORD."],
  ["Psalms 107","2 Let the redeemed of the LORD say [so], whom he hath redeemed from the hand of the enemy;","3 And gathered them out of the lands, from the east, and from the west, from the north, and from the south."],
  ["Psalms 107","8 Oh that [men] would praise the LORD [for] his goodness, and [for] his wonderful works to the children of men!"],
  ["Psalms 107","13 Then they cried unto the LORD in their trouble, [and] he saved them out of their distresses.","14 He brought them out of darkness and the shadow of death, and brake their bands in sunder."],
  ["Psalms 107","29 He maketh the storm a calm, so that the waves thereof are still.","30 Then are they glad because they be quiet; so he bringeth them unto their desired haven."],
  ["Psalms 108","12 Give us help from trouble: for vain [is] the help of man.","13 Through God we shall do valiantly: for he [it is that] shall tread down our enemies."],
  ["Psalms 109","21 But do thou for me, O GOD the Lord, for thy name's sake: because thy mercy [is] good, deliver thou me.","22 For I [am] poor and needy, and my heart is wounded within me."],
  ["Psalms 109","26 Help me, O LORD my God: O save me according to thy mercy:","27 That they may know that this [is] thy hand; [that] thou, LORD, hast done it."],
  ["Psalms 109","30 I will greatly praise the LORD with my mouth; yea, I will praise him among the multitude.","31 For he shall stand at the right hand of the poor, to save [him] from those that condemn his soul."],
  ["Psalms 110","3 Thy people [shall be] willing in the day of thy power, in the beauties of holiness from the womb of the morning: thou hast the dew of thy youth.","4 The LORD hath sworn, and will not repent, Thou [art] a priest for ever after the order of Melchizedek."],
  ["Psalms 111","1 Praise ye the LORD. I will praise the LORD with [my] whole heart, in the assembly of the upright, and [in] the congregation."],
  ["Psalms 111","2 The works of the LORD [are] great, sought out of all them that have pleasure therein.","3 His work [is] honourable and glorious: and his righteousness endureth for ever."],
  ["Psalms 111","4 He hath made his wonderful works to be remembered: the LORD [is] gracious and full of compassion."],
  ["Psalms 111","7 The works of his hands [are] verity and judgment; all his commandments [are] sure.","8 They stand fast for ever and ever, [and are] done in truth and uprightness."],
  ["Psalms 111","9 He sent redemption unto his people: he hath commanded his covenant for ever: holy and reverend [is] his name."],
  ["Psalms 111","10 The fear of the LORD [is] the beginning of wisdom: a good understanding have all they that do [his commandments]: his praise endureth for ever."],
  ["Psalms 112","1 Praise ye the LORD. Blessed [is] the man [that] feareth the LORD, [that] delighteth greatly in his commandments."],
  ["Psalms 112","7 He shall not be afraid of evil tidings: his heart is fixed, trusting in the LORD."],
  ["Psalms 113","2 Blessed be the name of the LORD from this time forth and for evermore.","3 From the rising of the sun unto the going down of the same the LORD'S name [is] to be praised."],
  ["Psalms 113","5 Who [is] like unto the LORD our God, who dwelleth on high,","6 Who humbleth [himself] to behold [the things that are] in heaven, and in the earth!"],
  ["Psalms 113","7 He raiseth up the poor out of the dust, [and] lifteth the needy out of the dunghill;","8 That he may set [him] with princes, [even] with the princes of his people."],
  ["Psalms 114","7 Tremble, thou earth, at the presence of the Lord, at the presence of the God of Jacob;","8 Which turned the rock [into] a standing water, the flint into a fountain of waters."],
  ["Psalms 115","1 Not unto us, O LORD, not unto us, but unto thy name give glory, for thy mercy, [and] for thy truth's sake."],
  ["Psalms 115","3 But our God [is] in the heavens: he hath done whatsoever he hath pleased."],
  ["Psalms 115","11 Ye that fear the LORD, trust in the LORD: he [is] their help and their shield."],
  ["Psalms 115","16 The heaven, [even] the heavens, [are] the LORD'S: but the earth hath he given to the children of men."],
  ["Psalms 115","17 The dead praise not the LORD, neither any that go down into silence.","18 But we will bless the LORD from this time forth and for evermore. Praise the LORD."],
  ["Psalms 116","1 I love the LORD, because he hath heard my voice [and] my supplications.","2 Because he hath inclined his ear unto me, therefore will I call upon [him] as long as I live."],
  ["Psalms 116","5 Gracious [is] the LORD, and righteous; yea, our God [is] merciful."],
  ["Psalms 116","7 Return unto thy rest, O my soul; for the LORD hath dealt bountifully with thee.","8 For thou hast delivered my soul from death, mine eyes from tears, [and] my feet from falling."],
  ["Psalms 116","12 What shall I render unto the LORD [for] all his benefits toward me?","13 I will take the cup of salvation, and call upon the name of the LORD."],
  ["Psalms 116","15 Precious in the sight of the LORD [is] the death of his saints."],
  ["Psalms 117","1 O praise the LORD, all ye nations: praise him, all ye people.","2 For his merciful kindness is great toward us: and the truth of the LORD [endureth] for ever. Praise ye the LORD."],
  ["Psalms 119","103 How sweet are thy words unto my taste! [yea, sweeter] than honey to my mouth!"],
  ["Proverbs 10","23 [It is] as sport to a fool to do mischief: but a man of understanding hath wisdom."],
  ["Proverbs 11","30 The fruit of the righteous [is] a tree of life; and he that winneth souls [is] wise."],
  ["Proverbs 11","4 Riches profit not in the day of wrath: but righteousness delivereth from death."],
  ["Proverbs 12","25 Heaviness in the heart of man maketh it stoop: but a good word maketh it glad."],
  ["Proverbs 14","7 Go from the presence of a foolish man, when thou perceivest not [in him] the lips of knowledge."],
  ["Proverbs 14","12 There is a way which seemeth right unto a man, but the end thereof [are] the ways of death."],
  ["Proverbs 15","13 A merry heart maketh a cheerful countenance: but by sorrow of the heart the spirit is broken."],
  ["Proverbs 17","22 A merry heart doeth good [like] a medicine: but a broken spirit drieth the bones."],
  ["Ecclesiastes 9","10 Whatsoever thy hand findeth to do, do [it] with thy might; for [there is] no work, nor device, nor knowledge, nor wisdom, in the grave, whither thou goest."],
  ["Ecclesiastes 9","17 The words of wise [men are] heard in quiet more than the cry of him that ruleth among fools."],
  ["Ecclesiastes 10","20 Curse not the king, no not in thy thought; and curse not the rich in thy bedchamber: for a bird of the air shall carry the voice, and that which hath wings shall tell the matter."],
  ["Ecclesiastes 12","13 Let us hear the conclusion of the whole matter: Fear God, and keep his commandments: for this [is] the whole [duty] of man."],
  ["Isaiah 46","13 I bring near my righteousness; it shall not be far off, and my salvation shall not tarry: and I will place salvation in Zion for Israel my glory."],
  ["Isaiah 52","10 The LORD hath made bare his holy arm in the eyes of all the nations; and all the ends of the earth shall see the salvation of our God."],
  ["Isaiah 53","3 He is despised and rejected of men; a man of sorrows, and acquainted with grief: and we hid as it were [our] faces from him; he was despised, and we esteemed him not."],
  ["Isaiah 53","5 But he [was] wounded for our transgressions, [he was] bruised for our iniquities: the chastisement of our peace [was] upon him; and with his stripes we are healed."],
  ["Isaiah 53","6 All we like sheep have gone astray; we have turned every one to his own way; and the LORD hath laid on him the iniquity of us all."],
  ["Isaiah 60","1 Arise, shine; for thy light is come, and the glory of the LORD is risen upon thee."],
  ["Isaiah 64","6 But we are all as an unclean [thing], and all our righteousnesses [are] as filthy rags; and we all do fade as a leaf; and our iniquities, like the wind, have taken us away."],
  ["Jeremiah 31","6 For there shall be a day, [that] the watchmen upon the mount Ephraim shall cry, Arise ye, and let us go up to Zion unto the LORD our God."],
  ["Lamentations 3","22 [It is of] the LORD'S mercies that we are not consumed, because his compassions fail not.","23 [They are] new every morning: great [is] thy faithfulness."],
  ["Lamentations 3","24 The LORD [is] my portion, saith my soul; therefore will I hope in him.","25 The LORD [is] good unto them that wait for him, to the soul [that] seeketh him."],
  ["Lamentations 3","26 [It is] good that [a man] should both hope and quietly wait for the salvation of the LORD."],
  ["Lamentations 3","31 For the Lord will not cast off for ever:","32 But though he cause grief, yet will he have compassion according to the multitude of his mercies.","33 For he doth not afflict willingly nor grieve the children of men."],
  ["Lamentations 3","34 To crush under his feet all the prisoners of the earth,","35 To turn aside the right of a man before the face of the most High,","36 To subvert a man in his cause, the Lord approveth not."],
  ["Lamentations 3","38 Out of the mouth of the most High proceedeth not evil and good?","39 Wherefore doth a living man complain, a man for the punishment of his sins?"],
  ["Lamentations 3","40 Let us search and try our ways, and turn again to the LORD.","41 Let us lift up our heart with [our] hands unto God in the heavens."],
  ["Lamentations 3","57 Thou drewest near in the day [that] I called upon thee: thou saidst, Fear not.","58 O Lord, thou hast pleaded the causes of my soul; thou hast redeemed my life."],
  ["Ezekiel 16","49 Behold, this was the iniquity of thy sister Sodom, pride, fulness of bread, and abundance of idleness was in her and in her daughters, neither did she strengthen the hand of the poor and needy."],
  ["Ezekiel 44","23 And they shall teach my people [the difference] between the holy and profane, and cause them to discern between the unclean and the clean."],
  ["Daniel 12","10 Many shall be purified, and made white, and tried; but the wicked shall do wickedly: and none of the wicked shall understand; but the wise shall understand."],
  ["Hosea 6","1 Come, and let us return unto the LORD: for he hath torn, and he will heal us; he hath smitten, and he will bind us up."],
  ["Joel 3","16 The LORD also shall roar out of Zion, and utter his voice from Jerusalem; and the heavens and the earth shall shake: but the LORD [will be] the hope of his people, and the strength of the children of Israel."],
  ["Amos 3","7 Surely the Lord GOD will do nothing, but he revealeth his secret unto his servants the prophets.","8 The lion hath roared, who will not fear? the Lord GOD hath spoken, who can but prophesy?"],
  ["Malachi 3","16 Then they that feared the LORD spake often one to another: and the LORD hearkened, and heard [it], and a book of remembrance was written before him for them that feared the LORD, and that thought upon his name."],
  ["Malachi 3","17 And they shall be mine, saith the LORD of hosts, in that day when I make up my jewels; and I will spare them, as a man spareth his own son that serveth him."],
  ["Malachi 3","18 Then shall ye return, and discern between the righteous and the wicked, between him that serveth God and him that serveth him not."],
  ["Matthew 1","21 And she shall bring forth a son, and thou shalt call his name JESUS: for he shall save his people from their sins."],
  ["Matthew 5","16 Let your light so shine before men, that they may see your good works, and glorify your Father which is in heaven."],
  ["Matthew 5","25 Agree with thine adversary quickly, whiles thou art in the way with him; lest at any time the adversary deliver thee to the judge, and the judge deliver thee to the officer, and thou be cast into prison."],
  ["Matthew 5","28 But I say unto you, That whosoever looketh on a woman to lust after her hath committed adultery with her already in his heart."],
  ["Matthew 6","24 No man can serve two masters: for either he will hate the one, and love the other; or else he will hold to the one, and despise the other. Ye cannot serve God and mammon."],
  ["Matthew 6","25 Therefore I say unto you, Take no thought for your life, what ye shall eat, or what ye shall drink; nor yet for your body, what ye shall put on. Is not the life more than meat, and the body than raiment?"],
  ["Matthew 7","15 Beware of false prophets, which come to you in sheep's clothing, but inwardly they are ravening wolves."],
  ["Matthew 8","11 And I say unto you, That many shall come from the east and west, and shall sit down with Abraham, and Isaac, and Jacob, in the kingdom of heaven."],
  ["Matthew 9","35 And Jesus went about all the cities and villages, teaching in their synagogues, and preaching the gospel of the kingdom, and healing every sickness and every disease among the people."],
  ["Matthew 11","28 Come unto me, all [ye] that labour and are heavy laden, and I will give you rest.","29 Take my yoke upon you, and learn of me; for I am meek and lowly in heart: and ye shall find rest unto your souls.","30 For my yoke [is] easy, and my burden is light."],
  ["Matthew 15","13 But he answered and said, Every plant, which my heavenly Father hath not planted, shall be rooted up."],
  ["Matthew 16","15 He saith unto them, But whom say ye that I am?","16 And Simon Peter answered and said, Thou art the Christ, the Son of the living God."],
  ["Matthew 19","23 Then said Jesus unto his disciples, Verily I say unto you, That a rich man shall hardly enter into the kingdom of heaven.","24 And again I say unto you, It is easier for a camel to go through the eye of a needle, than for a rich man to enter into the kingdom of God."],
  ["Mark 14","38 Watch ye and pray, lest ye enter into temptation. The spirit truly [is] ready, but the flesh [is] weak."],
  ["Mark 12","17 And Jesus answering said unto them, Render to Caesar the things that are Caesar's, and to God the things that are God's. And they marvelled at him."],
  ["Luke 1","46 And Mary said, My soul doth magnify the Lord,","47 And my spirit hath rejoiced in God my Saviour."],
  ["Luke 2","10 And the angel said unto them, Fear not: for, behold, I bring you good tidings of great joy, which shall be to all people.","11 For unto you is born this day in the city of David a Saviour, which is Christ the Lord."],
  ["Luke 4","4 And Jesus answered him, saying, It is written, That man shall not live by bread alone, but by every word of God."],
  ["Luke 8","16 No man, when he hath lighted a candle, covereth it with a vessel, or putteth [it] under a bed; but setteth [it] on a candlestick, that they which enter in may see the light."],
  ["Luke 8","17 For nothing is secret, that shall not be made manifest; neither [any thing] hid, that shall not be known and come abroad."],
  ["Luke 9","23 And he said to [them] all, If any [man] will come after me, let him deny himself, and take up his cross daily, and follow me."],
  ["Luke 9","24 For whosoever will save his life shall lose it: but whosoever will lose his life for my sake, the same shall save it."],
  ["Luke 9","26 For whosoever shall be ashamed of me and of my words, of him shall the Son of man be ashamed, when he shall come in his own glory, and [in his] Father's, and of the holy angels."],
  ["Luke 9","56 For the Son of man is not come to destroy men's lives, but to save [them]. And they went to another village."],
  ["John 1","1 In the beginning was the Word, and the Word was with God, and the Word was God.","2 The same was in the beginning with God.","3 All things were made by him; and without him was not any thing made that was made.","4 In him was life; and the life was the light of men."],
  ["John 3","16 For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.","17 For God sent not his Son into the world to condemn the world; but that the world through him might be saved."],
  ["John 10","27 My sheep hear my voice, and I know them, and they follow me:","28 And I give unto them eternal life; and they shall never perish, neither shall any [man] pluck them out of my hand."],
  ["John 20","31 But these are written, that ye might believe that Jesus is the Christ, the Son of God; and that believing ye might have life through his name."],
  ["Acts 2","21 And it shall come to pass, [that] whosoever shall call on the name of the Lord shall be saved."],
  ["Acts 3","19 Repent ye therefore, and be converted, that your sins may be blotted out, when the times of refreshing shall come from the presence of the Lord;"],
  ["Acts 4","13 Now when they saw the boldness of Peter and John, and perceived that they were unlearned and ignorant men, they marvelled; and they took knowledge of them, that they had been with Jesus."],
  ["Acts 17","29 Forasmuch then as we are the offspring of God, we ought not to think that the Godhead is like unto gold, or silver, or stone, graven by art and man's device."],
  ["Romans 1","16 For I am not ashamed of the gospel of Christ: for it is the power of God unto salvation to every one that believeth; to the Jew first, and also to the Greek.","17 For therein is the righteousness of God revealed from faith to faith: as it is written, The just shall live by faith."],
  ["Romans 3","23 For all have sinned, and come short of the glory of God;","24 Being justified freely by his grace through the redemption that is in Christ Jesus:"],
  ["Romans 5","8 But God commendeth his love toward us, in that, while we were yet sinners, Christ died for us.","9 Much more then, being now justified by his blood, we shall be saved from wrath through him."],
  ["Romans 6","23 For the wages of sin [is] death; but the gift of God [is] eternal life through Jesus Christ our Lord."],
  ["Romans 8","1 [There is] therefore now no condemnation to them which are in Christ Jesus, who walk not after the flesh, but after the Spirit."],
  ["Romans 8","7 Because the carnal mind [is] enmity against God: for it is not subject to the law of God, neither indeed can be.","8 So then they that are in the flesh cannot please God."],
  ["Romans 8","28 And we know that all things work together for good to them that love God, to them who are the called according to [his] purpose."],
  ["Romans 8","31 What shall we then say to these things? If God [be] for us, who [can be] against us?","32 He that spared not his own Son, but delivered him up for us all, how shall he not with him also freely give us all things?"],
  ["Romans 10","9 That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved.","10 For with the heart man believeth unto righteousness; and with the mouth confession is made unto salvation."],
  ["Romans 10","11 For the scripture saith, Whosoever believeth on him shall not be ashamed."],
  ["Romans 10","12 For there is no difference between the Jew and the Greek: for the same Lord over all is rich unto all that call upon him.","13 For whosoever shall call upon the name of the Lord shall be saved."],
  ["Romans 10","14 How then shall they call on him in whom they have not believed? and how shall they believe in him of whom they have not heard? and how shall they hear without a preacher?","15 And how shall they preach, except they be sent? as it is written, How beautiful are the feet of them that preach the gospel of peace, and bring glad tidings of good things!"],
  ["Romans 10","17 So then faith [cometh] by hearing, and hearing by the word of God."],
  ["Romans 12","1 I beseech you therefore, brethren, by the mercies of God, that ye present your bodies a living sacrifice, holy, acceptable unto God, [which is] your reasonable service."],
  ["Romans 12","2 And be not conformed to this world: but be ye transformed by the renewing of your mind, that ye may prove what [is] that good, and acceptable, and perfect, will of God."],
  ["Romans 12","21 Be not overcome of evil, but overcome evil with good."],
  ["Romans 14","4 Who art thou that judgest another man's servant? to his own master he standeth or falleth. Yea, he shall be holden up: for God is able to make him stand."],
  ["Romans 14","7 For none of us liveth to himself, and no man dieth to himself.","8 For whether we live, we live unto the Lord; and whether we die, we die unto the Lord: whether we live therefore, or die, we are the Lord's."],
  ["Romans 14","12 So then every one of us shall give account of himself to God."],
  ["Romans 14","13 Let us not therefore judge one another any more: but judge this rather, that no man put a stumblingblock or an occasion to fall in [his] brother's way."],
  ["Romans 14","14 I know, and am persuaded by the Lord Jesus, that [there is] nothing unclean of itself: but to him that esteemeth any thing to be unclean, to him [it is] unclean.","15 But if thy brother be grieved with [thy] meat, now walkest thou not charitably. Destroy not him with thy meat, for whom Christ died."],
  ["Romans 14","16 Let not then your good be evil spoken of:"],
  ["Romans 14","17 For the kingdom of God is not meat and drink; but righteousness, and peace, and joy in the Holy Ghost.","18 For he that in these things serveth Christ [is] acceptable to God, and approved of men."],
  ["Romans 14","19 Let us therefore follow after the things which make for peace, and things wherewith one may edify another."],
  ["Romans 14","21 [It is] good neither to eat flesh, nor to drink wine, nor [any thing] whereby thy brother stumbleth, or is offended, or is made weak.","22 Hast thou faith? have [it] to thyself before God. Happy [is] he that condemneth not himself in that thing which he alloweth."],
  ["Romans 15","3 For even Christ pleased not himself; but, as it is written, The reproaches of them that reproached thee fell on me."],
  ["Romans 15","4 For whatsoever things were written aforetime were written for our learning, that we through patience and comfort of the scriptures might have hope."],
  ["Romans 15","5 Now the God of patience and consolation grant you to be likeminded one toward another according to Christ Jesus:","6 That ye may with one mind [and] one mouth glorify God, even the Father of our Lord Jesus Christ."],
  ["Romans 16","17 Now I beseech you, brethren, mark them which cause divisions and offences contrary to the doctrine which ye have learned; and avoid them.","18 For they that are such serve not our Lord Jesus Christ, but their own belly; and by good words and fair speeches deceive the hearts of the simple."],
  ["I Corinthians 1","18 For the preaching of the cross is to them that perish foolishness; but unto us which are saved it is the power of God."],
  ["I Corinthians 3","11 For other foundation can no man lay than that is laid, which is Jesus Christ."],
  ["I Corinthians 5","11 But now I have written unto you not to keep company, if any man that is called a brother be a fornicator, or covetous, or an idolater, or a railer, or a drunkard, or an extortioner; with such an one no not to eat."],
  ["I Corinthians 6","13 Meats for the belly, and the belly for meats: but God shall destroy both it and them. Now the body [is] not for fornication, but for the Lord; and the Lord for the body."],
  ["I Corinthians 6","15 Know ye not that your bodies are the members of Christ? shall I then take the members of Christ, and make [them] the members of an harlot? God forbid."],
  ["I Corinthians 7","23 Ye are bought with a price; be not ye the servants of men.","24 Brethren, let every man, wherein he is called, therein abide with God."],
  ["I Corinthians 9","16 For though I preach the gospel, I have nothing to glory of: for necessity is laid upon me; yea, woe is unto me, if I preach not the gospel!"],
  ["I Corinthians 9","22 To the weak became I as weak, that I might gain the weak: I am made all things to all [men], that I might by all means save some."],
  ["I Corinthians 9","27 But I keep under my body, and bring [it] into subjection: lest that by any means, when I have preached to others, I myself should be a castaway."],
  ["I Corinthians 10","12 Wherefore let him that thinketh he standeth take heed lest he fall."],
  ["I Corinthians 10","13 There hath no temptation taken you but such as is common to man: but God [is] faithful, who will not suffer you to be tempted above that ye are able; but will with the temptation also make a way to escape, that ye may be able to bear [it]."],
  ["I Corinthians 13","1 Though I speak with the tongues of men and of angels, and have not charity, I am become [as] sounding brass, or a tinkling cymbal."],
  ["I Corinthians 13","4 Charity suffereth long, [and] is kind; charity envieth not; charity vaunteth not itself, is not puffed up,","5 Doth not behave itself unseemly, seeketh not her own, is not easily provoked, thinketh no evil;"],
  ["I Corinthians 15","50 Now this I say, brethren, that flesh and blood cannot inherit the kingdom of God; neither doth corruption inherit incorruption.","51 Behold, I shew you a mystery; We shall not all sleep, but we shall all be changed,"],
  ["I Corinthians 15","54 So when this corruptible shall have put on incorruption, and this mortal shall have put on immortality, then shall be brought to pass the saying that is written, Death is swallowed up in victory.","55 O death, where [is] thy sting? O grave, where [is] thy victory?"],
  ["I Corinthians 15","58 Therefore, my beloved brethren, be ye stedfast, unmoveable, always abounding in the work of the Lord, forasmuch as ye know that your labour is not in vain in the Lord."]
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