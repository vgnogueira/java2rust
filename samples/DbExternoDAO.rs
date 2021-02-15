#![allow(non_snake_case)]

use crate::java::sql::ResultSet;

#[derive(Debug)]
pub struct DbExternoDAO {
    db: FastDialerDB,
}
pub fn newDbExternoDAO (db: FastDialerDB) -> DbExternoDAO {
    DbExternoDAO {
        this.db = db;
        ;
    }
}

impl DbExternoDAO {

    pub fn get(&mut self, dbExternoId: i64) -> ResultSet {
        return db.query("select * from db_externo where id = "+dbExternoId);
    }
}
