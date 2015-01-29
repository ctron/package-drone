package de.dentrassi.pm.mail.web;

public class MailSettings
{
    private String username;

    private String password;

    private String host;

    private Integer port;

    public void setPort ( final Integer port )
    {
        this.port = port;
    }

    public Integer getPort ()
    {
        return this.port;
    }

    public String getUsername ()
    {
        return this.username;
    }

    public void setUsername ( final String username )
    {
        this.username = username;
    }

    public String getPassword ()
    {
        return this.password;
    }

    public void setPassword ( final String password )
    {
        this.password = password;
    }

    public String getHost ()
    {
        return this.host;
    }

    public void setHost ( final String host )
    {
        this.host = host;
    }

}
