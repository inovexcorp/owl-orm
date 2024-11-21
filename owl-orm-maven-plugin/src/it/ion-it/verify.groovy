/**
 * Groovy test for maven generation plugin execution.
 */

String base = "target/generated-sources/com/mica/eos/ontology"

File ecomm_package = new File(basedir, base + "/ecomm")
assert ecomm_package.isDirectory()
assert ecomm_package.listFiles().length == 12

File alert_pkg = new File(basedir, base + "/alert")
assert alert_pkg.isDirectory()
assert alert_pkg.listFiles().length == 3

File event_pkg = new File(basedir, base + "/event")
assert event_pkg.isDirectory()
assert event_pkg.listFiles().length == 10

File query_pkg = new File(basedir, base + "/query")
assert query_pkg.isDirectory()
assert query_pkg.listFiles().length == 5

File ref_pkg = new File(basedir, base + "/reference")
assert ref_pkg.isDirectory()
assert ref_pkg.listFiles().length == 20

File trade_pkg = new File(basedir, base + "/trade")
assert trade_pkg.isDirectory()
assert trade_pkg.listFiles().length == 10