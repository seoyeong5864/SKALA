drop table if exists orders;

create table orders (
    id         varchar(32) primary key,
    owner_id   varchar(32)   not null,
    item       varchar(100)  not null,
    status     varchar(20)   not null,
    ordered_at date,
    eta        date,
    cost       decimal(12,2)
);

insert into orders (id, owner_id, item, status, ordered_at, eta, cost) values
    ('12345', 'user1', '무선 이어폰',   'SHIPPING',  '2026-07-26', '2026-07-30',  52000),
    ('12346', 'user1', 'USB-C 케이블',  'DELIVERED', '2026-07-15', '2026-07-18',   4000),
    ('12347', 'user1', '기계식 키보드', 'PREPARING', '2026-07-28', '2026-08-01',  98000),
    ('12348', 'user1', '무선 키보드',   'SHIPPING',  '2026-07-20', '2026-07-24',  67000),
    ('99999', 'user2', '노트북 스탠드', 'PAID',      '2026-07-27', '2026-08-02',  21000);
