package de.dentrassi.pm.storage.channel.internal;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.dentrassi.pm.storage.channel.deploy.DeployGroup;
import de.dentrassi.pm.storage.channel.deploy.DeployKey;

public class DeployGroupTypeAdapter implements JsonSerializer<DeployGroup>, JsonDeserializer<DeployGroup>
{

    @Override
    public DeployGroup deserialize ( final JsonElement ele, final Type type, final JsonDeserializationContext ctx ) throws JsonParseException
    {
        final JsonObject obj = ele.getAsJsonObject ();

        return new DeployGroup ( obj.get ( "id" ).getAsString (), obj.get ( "name" ).getAsString (), null, group -> makeKeys ( group, obj, ctx ) );
    }

    private List<DeployKey> makeKeys ( final DeployGroup group, final JsonObject obj, final JsonDeserializationContext ctx )
    {
        final JsonElement keysEle = obj.get ( "keys" );
        if ( keysEle == null )
        {
            return Collections.emptyList ();
        }

        final JsonObject keys = keysEle.getAsJsonObject ();

        final List<DeployKey> result = new LinkedList<> ();

        for ( final Entry<String, JsonElement> entry : keys.entrySet () )
        {
            final JsonObject o2 = entry.getValue ().getAsJsonObject ();
            result.add ( new DeployKey ( group, entry.getKey (), o2.get ( "name" ).getAsString (), o2.get ( "key" ).getAsString (), getAsTimestamp ( ctx, o2 ) ) );
        }

        return result;
    }

    private static Instant getAsTimestamp ( final JsonDeserializationContext ctx, final JsonObject o2 )
    {
        final Object result = ctx.deserialize ( o2.get ( "timestamp" ), Date.class );
        if ( result == null )
        {
            return null;
        }
        return ( (Date)result ).toInstant ();
    }

    @Override
    public JsonElement serialize ( final DeployGroup group, final Type type, final JsonSerializationContext ctx )
    {
        final JsonObject obj = new JsonObject ();

        obj.addProperty ( "id", group.getId () );
        obj.addProperty ( "name", group.getName () );

        final JsonObject keys = new JsonObject ();
        obj.add ( "keys", keys );

        for ( final DeployKey key : group.getKeys () )
        {
            final JsonObject ok = new JsonObject ();
            ok.addProperty ( "name", key.getName () );
            ok.addProperty ( "key", key.getKey () );
            ok.add ( "timestamp", ctx.serialize ( Date.from ( key.getCreationTimestamp () ), Date.class ) );
            keys.add ( key.getId (), ok );
        }

        return obj;
    }

}
