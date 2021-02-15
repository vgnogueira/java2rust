#![allow(non_snake_case)]

pub fn main() {

}

#[derive(Debug)]
struct DAOFactory{}

impl DAOFactory {
    pub fn getHora(&self) -> String {
        return String::from("");
    }
}

#[derive(Debug)]
struct DateTime{}

#[derive(Debug)]
struct Integer{}

impl Integer {
    pub fn parseInt(stringToInt: &String) -> i32 {
        return 0;
    }
}

trait JavaString {
    fn substring(&self, start: i32, end: i32) -> String;
} 

impl JavaString for String {
    fn substring(&self, start: i32, end: i32) -> String {
        return String::from("substring");
    }
}

#[derive(Debug)]
pub struct GotoIfTime {
    checkTime: DateTime,
}
pub fn newGotoIfTime (daofactory: DAOFactory, tracklog: String) -> GotoIfTime {
    let agora: String = daofactory.getHora();
    let ano: i32 = Integer::parseInt(&agora.substring(0,4));
    let mes: i32 = Integer::parseInt(&agora.substring(5,7));
    let dia: i32 = Integer::parseInt(&agora.substring(8,10));
    let hora: i32 = Integer::parseInt(&agora.substring(11,13));
    let minuto: i32 = Integer::parseInt(&agora.substring(14,16));
    let segundo: i32 = Integer::parseInt(&agora.substring(17,19));    
    GotoIfTime {
        checkTime: newDateTime(ano,mes,dia,hora,minuto,segundo)
    }
}
// pub fn newGotoIfTime (checkTime: DateTime, tracklog: String) -> GotoIfTime {
//     GotoIfTime {
//         this.checkTime = checkTime;
//         ;
//     }
// }

// impl GotoIfTime {

//     pub fn monthToLong(&mut self, month: String) -> i32 {
//         match month {
//                 String::from("jan") => {
//                                     return 1;
//                 },
//                 String::from("feb") => {
//                                     return 2;
//                 },
//                 String::from("mar") => {
//                                     return 3;
//                 },
//                 String::from("apr") => {
//                                     return 4;
//                 },
//                 String::from("may") => {
//                                     return 5;
//                 },
//                 String::from("jun") => {
//                                     return 6;
//                 },
//                 String::from("jul") => {
//                                     return 7;
//                 },
//                 String::from("aug") => {
//                                     return 8;
//                 },
//                 String::from("sep") => {
//                                     return 9;
//                 },
//                 String::from("oct") => {
//                                     return 10;
//                 },
//                 String::from("nov") => {
//                                     return 11;
//                 },
//                 String::from("dec") => {
//                                     return 12;
//                 },
//                  _  => {
//                                     return 0;
//                 },

//         }
//     }

//     pub fn ast_check_time(&mut self, timeInfo: String) -> bool {
//         if timeInfo.equals("*|*|*|*") { 
//             {
//                 return true;
//             }
//         } 
//         let timeParts: String[] = timeInfo.split("\\|");
//         if timeParts.length != 4 { 
//             {
//                 return false;
//             }
//         } 
//         if !checkHour(timeParts[0]) { 
//             {
//                 return false;
//             }
//         } 
//         if !checkDayOfWeek(timeParts[1]) { 
//             {
//                 return false;
//             }
//         } 
//         if !checkDayOfMonth(timeParts[2]) { 
//             {
//                 return false;
//             }
//         } 
//         if !checkMonth(timeParts[3]) { 
//             {
//                 return false;
//             }
//         } 
//         return true;
//     }

//     pub fn dayOfWeekToLong(&mut self, dow: String) -> i32 {
//         if dow.equals("mon") { 
//             {
//                 return MONDAY;
//             }
//         } 
//         if dow.equals("tue") { 
//             {
//                 return TUESDAY;
//             }
//         } 
//         if dow.equals("wed") { 
//             {
//                 return WEDNESDAY;
//             }
//         } 
//         if dow.equals("thu") { 
//             {
//                 return THURSDAY;
//             }
//         } 
//         if dow.equals("fri") { 
//             {
//                 return FRIDAY;
//             }
//         } 
//         if dow.equals("sat") { 
//             {
//                 return SATURDAY;
//             }
//         } 
//         if dow.equals("sun") { 
//             {
//                 return SUNDAY;
//             }
//         } 
//         return  - 1;
//     }

//     pub fn checkHour(&mut self, timePart: String) -> bool {
//         if timePart.equals("*") { 
//             {
//                 return true;
//             }
//         } 
//         let timeRange: String[] = timePart.split("-");
//         let timeStart: String;
//         let timeEnd: String;
//          if timeRange.length == 1 { 
//             {
//                 timeStart = timeRange[0];
//                 ;
//                 timeEnd = timeStart;
//                 ;
//             }
//         }  else  if timeRange.length == 2 { 
//             {
//                 timeStart = timeRange[0];
//                 ;
//                 timeEnd = timeRange[1];
//                 ;
//             }
//         }  else {
//             return false;
//         }
//         let fmtTime: String = String.format("%02d:%02d",checkTime.getHourOfDay(),checkTime.getMinuteOfHour());
//          if timeStart.compareTo(timeEnd) <= 0 { 
//             {
//                 if timeStart.compareTo(fmtTime) > 0 { 
//                     {
//                         return false;
//                     }
//                 } 
//                 if timeEnd.compareTo(fmtTime) < 0 { 
//                     {
//                         return false;
//                     }
//                 } 
//             }
//         }  else {
//             let valido1: bool = timeStart.compareTo(fmtTime) <= 0;
//             valido1 = valido1&&fmtTime.compareTo("23:59")<=0;
//             ;
//             let valido2: bool = String::from("00:00").compareTo(fmtTime) <= 0;
//             valido2 = valido2&&fmtTime.compareTo(timeEnd)<=0;
//             ;
//             let valido: bool = valido1 || valido2;
//             if !valido { 
//                 {
//                     return false;
//                 }
//             } 
//         }
//         return true;
//     }

//     pub fn checkDayOfWeek(&mut self, timePart: String) -> bool {
//         if timePart.equals("*") { 
//             {
//                 return true;
//             }
//         } 
//         let days: String[] = timePart.split("-");
//         let dowStart: i32;
//         let dowEnd: i32;
//         let dow: i32 = checkTime.getDayOfWeek();
//          if days.length == 1 { 
//             {
//                 dowStart = dayOfWeekToLong(days[0]);
//                 ;
//                 dowEnd = dowStart;
//                 ;
//             }
//         }  else  if days.length == 2 { 
//             {
//                 dowStart = dayOfWeekToLong(days[0]);
//                 ;
//                 dowEnd = dayOfWeekToLong(days[1]);
//                 ;
//             }
//         }  else {
//             return false;
//         }
//          if dowStart <= dowEnd { 
//             {
//                 if dowStart <= dow && dow <= dowEnd { 
//                     {
//                         return true;
//                     }
//                 } 
//             }
//         }  else {
//             if dowStart <= dow && dow <= 7 || 1 <= dow && dow <= dowEnd { 
//                 {
//                     return true;
//                 }
//             } 
//         }
//         return false;
//     }

//     pub fn checkDayOfMonth(&mut self, timePart: String) -> bool {
//         if timePart.equals("*") { 
//             {
//                 return true;
//             }
//         } 
//         let days: String[] = timePart.split("-");
//         let domStart: i32;
//         let domEnd: i32;
//         let dom: i32 = checkTime.getDayOfMonth();
//          if days.length == 1 { 
//             {
//                 domStart = dayOfMonthToLong(days[0]);
//                 ;
//                 domEnd = domStart;
//                 ;
//             }
//         }  else  if days.length == 2 { 
//             {
//                 domStart = dayOfMonthToLong(days[0]);
//                 ;
//                 domEnd = dayOfMonthToLong(days[1]);
//                 ;
//             }
//         }  else {
//             return false;
//         }
//          if domStart <= domEnd { 
//             {
//                 if domStart <= dom && dom <= domEnd { 
//                     {
//                         return true;
//                     }
//                 } 
//             }
//         }  else {
//             if domStart <= dom && dom <= 31 || 1 <= dom && dom <= domEnd { 
//                 {
//                     return true;
//                 }
//             } 
//         }
//         return false;
//     }

//     pub fn dayOfMonthToLong(&mut self, day: String) -> i32 {
//         let m: i32;
//         /* try block */
//         {
//             m = Integer.parseInt(day);
//             ;
//         }
//         return m;
//     }

//     pub fn checkMonth(&mut self, timePart: String) -> bool {
//         if timePart.equals("*") { 
//             {
//                 return true;
//             }
//         } 
//         let days: String[] = timePart.split("-");
//         let monthStart: i32;
//         let monthEnd: i32;
//         let month: i32 = checkTime.getMonthOfYear();
//          if days.length == 1 { 
//             {
//                 monthStart = monthToLong(days[0]);
//                 ;
//                 monthEnd = monthStart;
//                 ;
//             }
//         }  else  if days.length == 2 { 
//             {
//                 monthStart = monthToLong(days[0]);
//                 ;
//                 monthEnd = monthToLong(days[1]);
//                 ;
//             }
//         }  else {
//             return false;
//         }
//          if monthStart <= monthEnd { 
//             {
//                 if monthStart <= month && month <= monthEnd { 
//                     {
//                         return true;
//                     }
//                 } 
//             }
//         }  else {
//             if monthStart <= month && month <= 12 || 1 <= month && month <= monthEnd { 
//                 {
//                     return true;
//                 }
//             } 
//         }
//         return false;
//     }
//}
