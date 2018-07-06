/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankofcanadaex;

/**
 *
 * @author sandeep.gainda
 */
public class Currency
{

    private String id;
    private String shortDescription;
    private String longDescription;

    
    public Currency(String id)
    {
        this.id = id;

    }

    public void setShortDescirption(String shortDescription)
    {
        this.shortDescription = shortDescription;
    }

    public void setLongDescription(String longDescription)
    {
        this.longDescription = longDescription;
    }

    public String getId()
    {
        return id;

    }

    public String getShortDescription()
    {
        return shortDescription;

    }

    public String getLongDescription()
    {
        return longDescription;

    }

}
