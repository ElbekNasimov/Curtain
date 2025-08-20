package com.example.curtain.constants;

public class Constants {

    public static final String [] userTypes = {
            "sklad",
            "dizayner",
            "bichuvchi",
            "admin",
            "superAdmin",
            "viewer",
    };
    public static final String [] userStatus = {
            "DISABLE",
            "ENABLE",
    };

    public static final String [] orderStatus = {
            "Yangi",
            "Tayyor",
            "Kesilmoqda",
            "Bichilmoqda",
            "Bichildi",
            "Topshirildi",
            "Yopildi",
    };
    public static final String [] extraOrder = {
            "Tanlang:",
            "Poshiv",
            "Ustanovka",
    };
    public static final String [] categories = {
            "Portyer",
            "Blekout",
            "Tyul",
            "Odnoton",
            "Aksessuar",
            "Bahrama",
            "Makaron",
            "Bubon",
            "Derjatel",
            "Karniz",
    };
    public static final String [] categories1 = {
            "Hammasi",
            "Portyer",
            "Blekout",
            "Tyul",
            "Odnoton",
            "Aksessuar",
            "Bahrama",
            "Makaron",
            "Bubon",
            "Derjatel",
            "Karniz",
    };
    // product location
    public static final String [] location = {
            "Sklad",
            "Salon Yunusobod",
            "Salon Nurafshon"
    };
    public static final String [] location1 = {
            "Tanlang:",
            "Sklad",
            "Salon Yunusobod",
            "Salon Nurafshon"
    };
    public static final String [] measurement = {
            "metr",
            "dona",
            "ro'lon",
            "pochka",
    };
    public static final String [] orderCats = {
            "Parda",
            "Dasturxon",
            "Gilam",
            "Chexol",
            "Ko'cha",
            "Stirka",
    };
    public static final String [] orderLocations = {
            "Toshkent sh",
            "Viloyat",
            "Chet el",
    };
    public static final String [] orderRooms = {
            "Xonani tanlang:",
            "Zal 1",
            "Zal 2",
            "Oshxona 1",
            "Oshxona 2",
            "Xol 1",
            "Xol 2",
            "Ofis 1",
            "Ofis 2",
            "Yotoqxona 1",
            "Yotoqxona 2",
            "Yotoqxona 3",
            "O'gil xona 1",
            "O'gil xona 2",
            "Qiz xona 1",
            "Qiz xona 2",
            "Mehmonxona 1",
            "Mehmonxona 2",
            "Mansarda",
            "Zina 1",
            "Zina 2",
            "Dush 1",
            "Dush 2",
            "Namozxona 1",
            "Namozxona 2",
            "Garderob 1",
            "Garderob 2",
            "Koridor 1",
            "Koridor 2",
    };
    public static final String [] orderRooms1 = {
            "Zal 1",
            "Zal 2",
            "Oshxona 1",
            "Oshxona 2",
            "Xol 1",
            "Xol 2",
            "Yotoqxona 1",
            "Yotoqxona 2",
            "Yotoqxona 3",
            "Ofis 1",
            "Ofis 2",
            "O'gil xona 1",
            "O'gil xona 2",
            "Qiz xona 1",
            "Qiz xona 2",
            "Mehmonxona 1",
            "Mehmonxona 2",
            "Mansarda",
            "Zina 1",
            "Zina 2",
            "Dush 1",
            "Dush 2",
            "Namozxona 1",
            "Namozxona 2",
            "Garderob 1",
            "Garderob 2",
            "Koridor 1",
            "Koridor 2",
    };
    public static final String [] xarajatStatusSpinner = {
            "Kassa",
            "Bonus",
    };
    public static final String [] objRooms = {
            "Etajni tanlang:",
            "Podval",
            "Etaj 1",
            "Etaj 2",
            "Etaj 3",
    };
    public static final String [] objRooms1 = {
            "Podval",
            "Etaj 1",
            "Etaj 2",
            "Etaj 3",
    };

    public static final String [] productColor = {
            "Ivory",
            "Krem",
            "Seriy",
            "Tilla",
            "Elektr",
            "Qora",
            "Roziviy",
            "Delfin",
            "Qizil",
            "Yashil",
            "Oq",
            "Sariq",
            "Korechniviy",
            "Fioletiviy",
    };
    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Agar input null yoki bo'sh bo'lsa, o'zgartirmasdan qaytar
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}