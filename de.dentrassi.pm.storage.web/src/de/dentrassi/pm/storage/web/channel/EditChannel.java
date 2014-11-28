package de.dentrassi.pm.storage.web.channel;

public class EditChannel
{
    private String id;

    /*
     * Validate duplicate name
     */
    private String name;

    public String getId ()
    {
        return this.id;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

}
