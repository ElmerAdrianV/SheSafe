She Safe App Design Project - README 
===

# She Safe

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
SheSafe is an application that seeks to help reduce the rate of crime against women by showing an updated map with reports of theft, kidnapping and any risk situation that a woman has ever lived near you.

### App Evaluation
- **Category:** Lifestyle/Safety
- **Story:** Allows users to share their experiences with risk situations in public places.
- **Market:** Anyone. The objective of the app is that all the women can avoid high risk places based in the past experiences shared in our database.
- **Habit:** Users can use the app every day several times. Our app can be used as a resource page to check the level of risk in a specific place, but it can also be used as route finder of the most secure route for way home.
- **Scope:** She Safe will start like a database where you can find all the reports of crimen related to womans. But, the objective of the app is turn into a consulting app where you can find any resource about safety in women and society.

## Product Spec

### 1. User Stories (Required and Optional)

For She Safe, we identified the following “must-have” features which a user needs to be able to perform for the app to work:

**Required Must-have Stories**

* User can login
* User can sign up
* User can see the latest report of risky situation of every location
* User can create a new report of risky situation in determined location
* User can get the historical of reports for every location  
* User can get a detail view of the reports of the risky situation
* User can access a list of useful resources as self-defense courses, law orientation or therapy


**Optional Nice-to-have Stories**
* User can press a SOS button and send all his emergency information via WhatsApp to his emergency contacts and police
* User can search places and see his historical reports
* User can see the latest report without internet (PersistSQL lite)
* User can see the nearlest user of his
* User can see the nearest users 
* User can receive notification of the newest reports nearest

### 2. Screen Archetypes

* Login Screen
    * User can login
* Sign up Screen
    * User can sign up
* Map 
    * User can see the latest report of risky situation of every location
* Report Form (modal overlay) 
    * User can create a new report of risky situation in determined location
* Historical Reports
    * User can get the historical of reports for every location  
* Report Detail View
    * User can get a detail view of the reports of the risky situation
* Resources Screen
    * * User can access a list of useful resources as self-defense courses, law orientation or therapy

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Map
* Resources Screen
* Report Form

**Flow Navigation** (Screen to Screen)

* Login screen ->Sign up Screen
   * Map
        * -> Historical reports -> Report Detail View
        * -> Resources screen


## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="https://raw.githubusercontent.com/ElmerAdrianV/SheSafe/main/Wireframes.jpeg" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
[This section will be completed in Unit 9]
### Models
[Add table of models]
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
