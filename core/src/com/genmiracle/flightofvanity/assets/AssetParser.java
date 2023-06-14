/*
 * AssetParser.java
 *
 * This is an interface for parsing a JSON entry into an asset. Without this interface, 
 * AssetDirectory is not that much different from a traditional AssetManager. We have
 * this feature to be customizable, in the same way that loaders are (though these are
 * orthogonal to loaders).  That way students can add their own custom asset definitions.
 *
 * @author Walker M. White
 * @data   04/20/2020
 */
package com.genmiracle.flightofvanity.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * This interface parses an asset of type {@code T} from a {@link JsonValue}.
 *
 * While it is easy to make a custom {@link AssetLoader} for any additional type of
 * asset, the complicated part is always specifying the initialization parameters.
 * This is where a JSON entry is idea.  In addition to the file, it can specify 
 * the asset parameters.  The purpose this interface is to take a {@link JsonValue}
 * and convert it into an asset of type {@code T}.
 *
 * This interface works like an iterator.  When {@link #reset} is called, it is
 * assigned a new {@link JsonValue}, which represents the root of the directory
 * structure.  Each time {@link #processNext} is called, it searches for the next
 * valid subtree (if there is one).  It then adds an asset to the asset manager
 * corresponding to the values of that subtree.
 *
 * In addition to creating an asset, the parser assigns proper keys to the assets.
 * Instead of relying on file names (which is brittle as these may change), the 
 * JSON file specifies keys for each asset.  This parser identifies these keys
 * and communicates the mapping from a key to the asset file name.
 *
 * This interface is critical to the proper performance of {@link AssetDirectory}.
 * Without a parser for a given type, the asset directory can still load that type
 * manually, but it cannot automatically process the asset from a JSON entry.
 */
public interface AssetParser<T> {

    /**
     * Returns the asset type generated by this parser
     *
     * @return the asset type generated by this parser
     */
    public Class<T> getType();

    /**
     * Resets the parser iterator for the given directory.
     *
     * The value directory is assumed to be the root of a larger JSON structure.
     * The individual assets are defined by subtrees in this structure.
     *
     * @param directory    The JSON representation of the asset directory
     */
    public void reset(JsonValue directory);

    /**
     * Returns true if there are still assets left to generate
     *
     * @return true if there are still assets left to generate
     */
    public boolean hasNext();

    /**
     * Processes the next available asset, loading it into the asset manager
     *
     * In addition to loading the asset into the asset manager, this method 
     * updates keymap with the key-filename assocation. This is what allows
     * {@link AssetDirectory#getEntry} to work.
     *
     * This method fails silently if there are no available assets to process.
     *
     * @param manager    The asset manager to load an asset
     * @param keymap    The mapping of JSON keys to asset file names
     */
    public void processNext(AssetManager manager, ObjectMap<String, String> keymap);

}


