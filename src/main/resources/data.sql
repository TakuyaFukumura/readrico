DELETE FROM reading_record;
INSERT INTO reading_record (title, author, reading_status, current_page, total_pages, summary, thoughts) VALUES
('吾輩は猫である', '夏目漱石', 'READING', 150, 300, '明治時代の教師の家で飼われている猫の視点から描かれた風刺小説', '猫の視点が面白く、当時の社会を鋭く描写している'),
('坊っちゃん', '夏目漱石', 'COMPLETED', 200, 200, '江戸っ子気質の青年教師が四国の中学校で奮闘する物語', '主人公の真っ直ぐな性格が魅力的。痛快な読み物'),
('銀河鉄道の夜', '宮沢賢治', 'UNREAD', 0, 180, '', ''),
('こころ', '夏目漱石', 'PAUSED', 80, 250, '明治末期の知識人の心の葛藤を描いた作品', '重いテーマだが考えさせられる内容'),
('羅生門', '芥川龍之介', 'COMPLETED', 30, 30, '平安時代末期を舞台にした短編小説', '短いながらも印象深い作品。人間のエゴイズムを描いている');
