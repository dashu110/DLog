package com.lucky.lklogger.lcmail.factory;

public class DeafultEmailProperty extends EmailFactory {

    private String type;

    public DeafultEmailProperty(String type) {
        this.type = type;
    }

    @Override
    public EmailProperty createEmailProperty() {
        if (type.equals("outlook"))
            return new EmailProperty("smtp.luckincoffee.com", 587, 995);
        else if (type.equals("ucar"))
            return new EmailProperty("smtp.ucarinc.com", 587, 993);
        else if (type.equals("163"))
            return new EmailProperty("smtp.163.com", 25, 465);
        else if (type.equals("qq"))
            return new EmailProperty("smtp.qq.com", 25, 465);
        else if (type.equals("sina"))
            return new EmailProperty("smtp.sina.com", 25, 465);
        else
            return new EmailProperty("smtp.luckincoffee.com", 587, 995);
    }
}
