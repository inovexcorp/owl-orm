/**
 * Groovy test for maven generation plugin execution.
 */

File bieronto = new File(basedir, "target/generated-sources/com/realmone/bieronto")
assert bieronto.isDirectory()
assert bieronto.listFiles().length == 19
File foaf = new File(basedir, "target/generated-sources/com/realmone/foaf")
assert foaf.isDirectory()
assert foaf.listFiles().length == 15