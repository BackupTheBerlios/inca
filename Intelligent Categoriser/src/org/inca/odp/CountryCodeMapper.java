/*
 * Created on Dec 16, 2004 11:50:30 PM
 */
package org.inca.odp;

import java.util.Hashtable;

/**
 * @author achim
 */
public class CountryCodeMapper {
    private static Hashtable LANGUAGE_TO_CODE = new Hashtable();

    static {
        LANGUAGE_TO_CODE.put("Abkhazian", "ab");
        LANGUAGE_TO_CODE.put("Afar", "aa");
        LANGUAGE_TO_CODE.put("Afrikaans", "af");
        LANGUAGE_TO_CODE.put("Akan", "ak");
        LANGUAGE_TO_CODE.put("Albanian", "sq");
        LANGUAGE_TO_CODE.put("Amharic", "am");
        LANGUAGE_TO_CODE.put("Arabic", "ar");
        LANGUAGE_TO_CODE.put("Aragonese", "an");
        LANGUAGE_TO_CODE.put("Armenian", "hy");
        LANGUAGE_TO_CODE.put("Assamese", "as");
        LANGUAGE_TO_CODE.put("Avaric", "av");
        LANGUAGE_TO_CODE.put("Avestan", "ae");
        LANGUAGE_TO_CODE.put("Aymara", "ay");
        LANGUAGE_TO_CODE.put("Azerbaijani", "az");
        LANGUAGE_TO_CODE.put("Bambara", "bm");
        LANGUAGE_TO_CODE.put("Bashkir", "ba");
        LANGUAGE_TO_CODE.put("Basque", "eu");
        LANGUAGE_TO_CODE.put("Belarusian", "be");
        LANGUAGE_TO_CODE.put("Bengali", "bn");
        LANGUAGE_TO_CODE.put("Bihari", "bh");
        LANGUAGE_TO_CODE.put("Bislama", "bi");
        LANGUAGE_TO_CODE.put("Bosnian", "bs");
        LANGUAGE_TO_CODE.put("Breton", "br");
        LANGUAGE_TO_CODE.put("Bulgarian", "bg");
        LANGUAGE_TO_CODE.put("Burmese", "my");
        LANGUAGE_TO_CODE.put("Chamorro", "ch");
        LANGUAGE_TO_CODE.put("Chechen", "ce");
        LANGUAGE_TO_CODE.put("Chinese", "zh");
        LANGUAGE_TO_CODE.put("Church", "Slavonic cu");
        LANGUAGE_TO_CODE.put("Church", "cu");
        LANGUAGE_TO_CODE.put("Chuvash", "cv");
        LANGUAGE_TO_CODE.put("Cornish", "kw");
        LANGUAGE_TO_CODE.put("Corsican", "co");
        LANGUAGE_TO_CODE.put("Cree", "cr");
        LANGUAGE_TO_CODE.put("Croatian", "hr");
        LANGUAGE_TO_CODE.put("Czech", "cs");
        LANGUAGE_TO_CODE.put("Danish", "da");
        LANGUAGE_TO_CODE.put("Divehi", "dv");
        LANGUAGE_TO_CODE.put("Dzongkha", "dz");
        LANGUAGE_TO_CODE.put("English", "en");
        LANGUAGE_TO_CODE.put("Esperanto", "eo");
        LANGUAGE_TO_CODE.put("Estonian", "et");
        LANGUAGE_TO_CODE.put("Ewe", "ee");
        LANGUAGE_TO_CODE.put("Faroese", "fo");
        LANGUAGE_TO_CODE.put("Fijian", "fj");
        LANGUAGE_TO_CODE.put("Finnish", "fi");
        LANGUAGE_TO_CODE.put("French", "fr");
        LANGUAGE_TO_CODE.put("Frisian", "fy");
        LANGUAGE_TO_CODE.put("Fulah", "ff");
        LANGUAGE_TO_CODE.put("Gallegan", "gl");
        LANGUAGE_TO_CODE.put("Ganda", "lg");
        LANGUAGE_TO_CODE.put("Georgian", "ka");
        LANGUAGE_TO_CODE.put("German", "de");
        LANGUAGE_TO_CODE.put("Greenlandic", "kl");
        LANGUAGE_TO_CODE.put("Guarani", "gn");
        LANGUAGE_TO_CODE.put("Gujarati", "gu");
        LANGUAGE_TO_CODE.put("Haitian", "ht");
        LANGUAGE_TO_CODE.put("Hausa", "ha");
        LANGUAGE_TO_CODE.put("Hebrew", "he");
        LANGUAGE_TO_CODE.put("Herero", "hz");
        LANGUAGE_TO_CODE.put("Hindi", "hi");
        LANGUAGE_TO_CODE.put("Hiri", "ho");
        LANGUAGE_TO_CODE.put("Hungarian", "hu");
        LANGUAGE_TO_CODE.put("Icelandic", "is");
        LANGUAGE_TO_CODE.put("Ido", "io");
        LANGUAGE_TO_CODE.put("Igbo", "ig");
        LANGUAGE_TO_CODE.put("Indonesian", "id");
        LANGUAGE_TO_CODE.put("Auxiliary", "ia");
        LANGUAGE_TO_CODE.put("Interlingue", "ie");
        LANGUAGE_TO_CODE.put("Inuktitut", "iu");
        LANGUAGE_TO_CODE.put("Inupiaq", "ik");
        LANGUAGE_TO_CODE.put("Irish", "ga");
        LANGUAGE_TO_CODE.put("Italian", "it");
        LANGUAGE_TO_CODE.put("Japanese", "ja");
        LANGUAGE_TO_CODE.put("Javanese", "jv");
        LANGUAGE_TO_CODE.put("Kannada", "kn");
        LANGUAGE_TO_CODE.put("Kanuri", "kr");
        LANGUAGE_TO_CODE.put("Kashmiri", "ks");
        LANGUAGE_TO_CODE.put("Kazakh", "kk");
        LANGUAGE_TO_CODE.put("Khmer", "km");
        LANGUAGE_TO_CODE.put("Kinyarwanda", "rw");
        LANGUAGE_TO_CODE.put("Kirghiz", "ky");
        LANGUAGE_TO_CODE.put("Komi", "kv");
        LANGUAGE_TO_CODE.put("Kongo", "kg");
        LANGUAGE_TO_CODE.put("Korean", "ko");
        LANGUAGE_TO_CODE.put("Kuanyama", "kj");
        LANGUAGE_TO_CODE.put("Kurdish", "ku");
        LANGUAGE_TO_CODE.put("Kwanyama", "kj");
        LANGUAGE_TO_CODE.put("Lao", "lo");
        LANGUAGE_TO_CODE.put("Latin", "la");
        LANGUAGE_TO_CODE.put("Latvian", "lv");
        LANGUAGE_TO_CODE.put("Lingala", "ln");
        LANGUAGE_TO_CODE.put("Lithuanian", "lt");
        LANGUAGE_TO_CODE.put("Luba-Katanga", "lu");
        LANGUAGE_TO_CODE.put("Macedonian", "mk");
        LANGUAGE_TO_CODE.put("Malagasy", "mg");
        LANGUAGE_TO_CODE.put("Malay", "ms");
        LANGUAGE_TO_CODE.put("Malayalam", "ml");
        LANGUAGE_TO_CODE.put("Maltese", "mt");
        LANGUAGE_TO_CODE.put("Manx", "gv");
        LANGUAGE_TO_CODE.put("Maori", "mi");
        LANGUAGE_TO_CODE.put("Marathi", "mr");
        LANGUAGE_TO_CODE.put("Marshallese", "mh");
        LANGUAGE_TO_CODE.put("Moldavian", "mo");
        LANGUAGE_TO_CODE.put("Mongolian", "mn");
        LANGUAGE_TO_CODE.put("Nauru", "na");
        LANGUAGE_TO_CODE.put("Ndonga", "ng");
        LANGUAGE_TO_CODE.put("Nepali", "ne");
        LANGUAGE_TO_CODE.put("Northern", "se");
        LANGUAGE_TO_CODE.put("North", "nd");
        LANGUAGE_TO_CODE.put("Norwegian", "no");
        LANGUAGE_TO_CODE.put("Norwegian", "nb");
        LANGUAGE_TO_CODE.put("Norwegian", "nn");
        LANGUAGE_TO_CODE.put("Occitan", "oc");
        LANGUAGE_TO_CODE.put("Ojibwa", "oj");
        LANGUAGE_TO_CODE.put("Old", "cu");
        LANGUAGE_TO_CODE.put("Old", "cu");
        LANGUAGE_TO_CODE.put("Old", "cu");
        LANGUAGE_TO_CODE.put("Oriya", "or");
        LANGUAGE_TO_CODE.put("Oromo", "om");
        LANGUAGE_TO_CODE.put("Pali", "pi");
        LANGUAGE_TO_CODE.put("Persian", "fa");
        LANGUAGE_TO_CODE.put("Polish", "pl");
        LANGUAGE_TO_CODE.put("Portuguese", "pt");
        LANGUAGE_TO_CODE.put("Pushto", "ps");
        LANGUAGE_TO_CODE.put("Quechua", "qu");
        LANGUAGE_TO_CODE.put("Raeto-Romance", "rm");
        LANGUAGE_TO_CODE.put("Romanian", "ro");
        LANGUAGE_TO_CODE.put("Rundi", "rn");
        LANGUAGE_TO_CODE.put("Russian", "ru");
        LANGUAGE_TO_CODE.put("Samoan", "sm");
        LANGUAGE_TO_CODE.put("Sango", "sg");
        LANGUAGE_TO_CODE.put("Sanskrit", "sa");
        LANGUAGE_TO_CODE.put("Sardinian", "sc");
        LANGUAGE_TO_CODE.put("Scottish", "gd");
        LANGUAGE_TO_CODE.put("Serbian", "sr");
        LANGUAGE_TO_CODE.put("Shona", "sn");
        LANGUAGE_TO_CODE.put("Sichuan", "ii");
        LANGUAGE_TO_CODE.put("Sindhi", "sd");
        LANGUAGE_TO_CODE.put("Slovak", "sk");
        LANGUAGE_TO_CODE.put("Slovenian", "sl");
        LANGUAGE_TO_CODE.put("Somali", "so");
        LANGUAGE_TO_CODE.put("South", "nr");
        LANGUAGE_TO_CODE.put("Spanish", "es");
        LANGUAGE_TO_CODE.put("Sundanese", "su");
        LANGUAGE_TO_CODE.put("Swahili", "sw");
        LANGUAGE_TO_CODE.put("Swati", "ss");
        LANGUAGE_TO_CODE.put("Swedish", "sv");
        LANGUAGE_TO_CODE.put("Tagalog", "tl");
        LANGUAGE_TO_CODE.put("Tahitian", "ty");
        LANGUAGE_TO_CODE.put("Tajik", "tg");
        LANGUAGE_TO_CODE.put("Tamil", "ta");
        LANGUAGE_TO_CODE.put("Tatar", "tt");
        LANGUAGE_TO_CODE.put("Telugu", "te");
        LANGUAGE_TO_CODE.put("Thai", "th");
        LANGUAGE_TO_CODE.put("Tibetan", "bo");
        LANGUAGE_TO_CODE.put("Tigrinya", "ti");
        LANGUAGE_TO_CODE.put("Tonga", "to");
        LANGUAGE_TO_CODE.put("Tsonga", "ts");
        LANGUAGE_TO_CODE.put("Tswana", "tn");
        LANGUAGE_TO_CODE.put("Turkish", "tr");
        LANGUAGE_TO_CODE.put("Turkmen", "tk");
        LANGUAGE_TO_CODE.put("Twi", "tw");
        LANGUAGE_TO_CODE.put("Ukrainian", "uk");
        LANGUAGE_TO_CODE.put("Urdu", "ur");
        LANGUAGE_TO_CODE.put("Uzbek", "uz");
        LANGUAGE_TO_CODE.put("Venda", "ve");
        LANGUAGE_TO_CODE.put("Vietnamese", "vi");
        LANGUAGE_TO_CODE.put("Volapk", "vo");
        LANGUAGE_TO_CODE.put("Walloon", "wa");
        LANGUAGE_TO_CODE.put("Welsh", "cy");
        LANGUAGE_TO_CODE.put("Wolof", "wo");
        LANGUAGE_TO_CODE.put("Xhosa", "xh");
        LANGUAGE_TO_CODE.put("Yiddish", "yi");
        LANGUAGE_TO_CODE.put("Yoruba", "yo");
        LANGUAGE_TO_CODE.put("Zulu", "zu");
    }
    
    public static String getCountryCode(String language) {
        return (String)LANGUAGE_TO_CODE.get(language);
    }
}