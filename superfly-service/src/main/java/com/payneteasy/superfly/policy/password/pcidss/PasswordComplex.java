package com.payneteasy.superfly.policy.password.pcidss;

import com.payneteasy.superfly.policy.IPolicy;
import com.payneteasy.superfly.policy.PolicyException;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 17:13:32
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordComplex implements IPolicy<PasswordCheckContext> {


    public void apply(PasswordCheckContext aContext) throws PolicyException {
        if(aContext.getPassword()==null)
            throw new PolicyException(PolicyException.EMPTY_PASSWORD);

//Факторы сложности
        int has_only_numeric = 0;
        boolean has_numeric = false;
        boolean has_lower = false;
        boolean has_upper = false;
        boolean has_signs = false;


        //Проверяем наличие определенной группы символов
               for(int i = 0; i < aContext.getPassword().length(); i++)
               {
                   has_numeric |= Character.isDigit(aContext.getPassword().charAt(i));
                   has_lower   |= Character.isLowerCase(aContext.getPassword().charAt(i));
                   has_upper   |= Character.isUpperCase(aContext.getPassword().charAt(i));
                   has_signs   |= signs.indexOf(aContext.getPassword().charAt(i)) >= 0;

                   if (has_numeric)
                       has_only_numeric++;
               }


        //Вычисляем самое частое состояние - пароль состоит из одних лишь цифр
               if (has_only_numeric == aContext.getPassword().length() && !has_lower && !has_upper && !has_signs)
               {
                   current_complexity = 0.40;
                   has_only_numeric = 0;
                   has_numeric = false;
                   has_lower= false;
                   has_upper = false;
                   has_signs = false;
                   throw new PolicyException(PolicyException.SIMPLE_PASSWORD);
               }
        

//Сбрасываем
        has_only_numeric = 0;

        //Изменяем комплексность в соответствии с паролем
        if (has_lower || has_upper)
        {
            current_complexity = current_complexity + 0.10;
        }

        if (has_lower && has_upper)
        {
            current_complexity = current_complexity + 0.15;
        }

        if (has_signs)
        {
            current_complexity = current_complexity + 0.15;
        }


        if(!has_numeric && (!has_lower || !has_upper)){
            throw new PolicyException(PolicyException.SIMPLE_PASSWORD);
        }
    }


    protected final String signs = "~`!@#$%^&*+-=_|\\/()[]{}<>,.;:?\"\'";
    protected double current_complexity=0;

}