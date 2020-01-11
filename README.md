# teams-connector-clj

A simple connector for [Records 365](https://www.recordpoint.com/records365/) that allows Microsoft Teams to be used as a content source.

## Setup

### Install Clojure & Leiningen

Follow the instructions appropriate to your platform [here](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools).

Once you have Clojure installed, you will need to install Leiningen, Clojure's build tool. Follow the instructions [here](https://leiningen.org/).

 

## Usage

Create a new config file and update its values:

    $ mv lein-env.sample .lein-env
    
Ensure it's all working by running the tests:    

    $ lein test
    
Run the application:

    $ lein run
    
For examples on how to use the API, see `resources/example.clj`    

## License

Copyright © 2019 Leonardo Borges

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
